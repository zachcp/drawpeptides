(ns drawpeptides.core
  (:gen-class)
  (:require [mikera.image.core :refer (rotate load-image show save)])
  (:require [mikera.image.spectrum :refer (wheel)])
  (:require [gifclj.core :refer (write-gif)])
  (:import [org.openscience.cdk.interfaces IAtomContainer])
  (:import [org.openscience.cdk Atom Bond AminoAcid AtomContainer])
  (:import [org.openscience.cdk.layout StructureDiagramGenerator])
  (:import [org.openscience.cdk.renderer.color UniColor])
  (:import [org.openscience.cdk.renderer.generators.standard StandardGenerator])
  (:import [org.openscience.cdk.renderer.visitor AWTDrawVisitor])
  (:import [org.openscience.cdk.silent SilentChemObjectBuilder])
  (:import [org.openscience.cdk.smiles SmilesParser])
  (:import [org.openscience.cdk.renderer AtomContainerRenderer SymbolVisibility])
  (:import [org.openscience.cdk.renderer.font AWTFontManager])
  (:import [org.openscience.cdk.renderer.generators BasicSceneGenerator])
  (:import [org.openscience.cdk.renderer.generators.standard StandardGenerator
            StandardGenerator$AnnotationColor
            StandardGenerator$AnnotationFontScale
            StandardGenerator$AnnotationDistance
            StandardGenerator$AtomColor
            StandardGenerator$BondSeparation
            StandardGenerator$FancyBoldWedges
            StandardGenerator$FancyHashedWedges
            StandardGenerator$HashSpacing
            StandardGenerator$Highlighting
            StandardGenerator$HighlightStyle
            StandardGenerator$OuterGlowWidth
            StandardGenerator$StrokeRatio
            StandardGenerator$SymbolMarginRatio
            StandardGenerator$Visibility
            StandardGenerator$WaveSpacing
            StandardGenerator$WedgeRatio])
  (:import [java.awt Color Font Rectangle])
  (:import [java.awt.image BufferedImage])
  (:import [java.util ArrayList]))



;Data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def AminoAcids
  "map of substrate molecules and their smiles string"
  {;Canonical amino acida
   :ALA    "NC(C)C(=O)"
   :GLY    "NCC(=O)"
   :SER    "NC(CO)C(=O)"
   :THR    "NC(C(C)O)C(=O)"
   :CYS    "NC(CS)C(=O)"
   :VAL    "NC(C(C)C)C(=O)"
   :LEU    "NC(CC(C)C)C(=O)"
   :ILE    "NC(C(C)CC)C(=O)"
   :MET    "NC(CCSC)C(=O)"
   :PRO    "N1C(C(=O)O)CCC1"
   :PHE    "NC(Cc1ccccc1)C(=O)"
   :TYR    "NC(Cc1cc(O)ccc1)C(=O)"
   :TRP    "N[C@@](CC1=CNC2=C1C=CC=C2)C(=O)"
   :ASP    "NC(CC(=O)O)C(=O)"
   :GLU    "NC(CCC(=O)O)C(=O)"
   :ASN    "NC(CC(=O)N)C(=O)"
   :GLN    "NC(CCC(=O)N)C(=O)"
   :HIS    "N[C@@H](CC1=CN=CN1)C(=O)"
   :LYS    "NC(CCCCN)C(=O)"
   :ARG    "NC(CCCNC(=N)N)C(=O)"
   ;Alternative AminoAcids in Secondary Metabolite Pathways
   :3meGLU "N[C@@H](C(C)CC(=O))C(=O)"
   :4mHA   "N1CC(CCC)CC1C(=O)"
   :4pPro  "N[C@@H](C(C)CC(=O))C(=O)"
   :AAA    "NC(CCCC(=O)O)C(=O)"
   :AAD    "N[C@@H](CCCC(=O)O)C(=O)"
   :ABU    "N[C@@H](C(C))C(=O)"
   ;:ADDS   "NC(/C=C/C(/C)=C/C(C)C(OC)CCc1ccccc1)C(C)C(=O)"
   :AEO    "N[C@@H](CCCCCC(=O)C1OC1)C(=O)"
   :bALA   "N[C@@H]CC(=O)"
   :dALA   "N[C@@H](C)C(=O)"
   :BHT    "N[C@@H](C(O)c1ccc(O)cc1)C(=O)"
   :BLYS   "NCCCC(N)CC(=O)"
   :BMT    "NC(C(O)C(C)C/C=C/C)C(=O)"
   :DAB    "N[C@@H](CCN)C(=O)"
   :DHA    "NC(=C)C(=O)"
   :DHPG   "N[C@@H](c1cc(O)cc(O)c1)C(=O)"
   :DHT    "NC(C(=O)C)C(=O)"
   :HPG    "N[C@@H](c1ccc(O)cc1)C(=O)"
   :IVA    "N[C@@H](CC)(C)C(=O)"
   :MPRO   "N1[C@@H](CC(C)C1)C(=O)"
   :ORN    "N[C@@H](CCCN)C(=O)"
   :PGLY   "N[C@@H](c1ccccc1)C(=O)"
   :PHG    "N[C@@H](c1ccccc1)C(=O)"
   :PICO   "n1ccccc1C(=O)"
   :PIP    "N1[CH]CCCC1C(=O)"
   :TCL    "N[C@@H](CC(C)C(Cl)(Cl)(Cl))C(=O)"
   :VOL    "N[C@@H](C(C)C)CO"})

; Core Functions for Parsing and handling AminoAcids
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-2D [^AtomContainer mol]
  "generate coordinates for an AtomContainer"
    ^AtomContainer
   (let [sdg (doto (StructureDiagramGenerator.)
               (.setMolecule mol)
               (.generateCoordinates))
         mol2d (.getMolecule sdg)]
     mol2d))

(defn parsesmiles [smiles]
  "return an IAtomContainer from smiles input"
  (let [builder (SilentChemObjectBuilder/getInstance)
        sp (SmilesParser. builder)
        mol (.parseSmiles sp smiles)]
    (get-2D mol)))


(defn- ->AminoAcid [mol]
  "take an AtomContainer and makes an AminoAcid
  the main difference is that AminoAcids have a few extra definitiions that will
  be used later that allow finding the N and C termini"
  {:pre [(= "N" (.getSymbol (first (.atoms mol))))
         (= "C" (last (butlast (.atoms mol))))]}
  (let [AA (new AminoAcid)
        atoms (seq (.atoms mol))
        nterm (.hashCode (first atoms))
        cterm (.hashCode (last (butlast atoms)))]

    ;add atoms
    (doseq [a atoms]
      (cond
        (= nterm (.hashCode a)) (.addNTerminus AA a)
        (= cterm (.hashCode a)) (.addCTerminus AA a)
        :else (.addAtom AA a)))

    ;add bonds
    (doseq [b (.bonds mol)] (.addBond AA b))

    ;remove H from C-Terminus
    (doto (.getCTerminus AA)
      (.setImplicitHydrogenCount (java.lang.Integer. 0)))

    AA))

(defn- get-AA [key]
  "make a CDK AminoAcid from the AminoAcid data.
  requires a key and will parse the value."
  {:pre [(keyword? key)
         (get AminoAcids key)]}
  (let [mol (parsesmiles (get AminoAcids key))]
    (->AminoAcid mol)))

(def nicecolors
  "from CDK Depict API
  https://github.com/cdk/cdk/blob/master/app/depict/src/main/java/org/openscience/cdk/depict/DepictionGenerator.java
  "
  (map #(Color. %) [0x00538A,0x93AA00, 0xC10020, 0xFFB300 0x007D34 0xFF6800 0xCEA262 0x817066
                    0xA6BDD7 0x803E75 0xF6768E 0xFF7A5C 0x53377A 0xFF8E00 0xB32851 0xF4C800
                    0x7F180D 0x593315 0xF13A13 0x232C16]))

(defn makepeptide [aminoacids]
  "take a sequence of keywords corresponding to AminoAcids and link them up"
  ^IAtomContainer
  (let [;make peptide from AminoAcids
        aaseq (map get-AA aminoacids)
        cterms (map #(.getCTerminus %) aaseq)
        nterms (map #(.getNTerminus %) aaseq)
        bondstomake (partition 2 (interleave (rest nterms) (butlast cterms)))
        bonds (map (fn [[a b]] (Bond. a b)) bondstomake)
        colors nicecolors
        AC (new AtomContainer)
        ;get data about each atom in the final dataset
        ;atomsets (apply hash-map (interleave (map gethashcodes aaseq) aminoacids))
        ;atomdata (make-peratom-data atomsets)
        aacount (atom 1)]

    ;add all of the atoms and all of the bond
    (doseq [aa aaseq]
      (do
        ;add all atoms
        (doseq [atm (.atoms aa)]
          (do
            (doto atm (.setProperty StandardGenerator/HIGHLIGHT_COLOR (nth colors @aacount)))
            (doto AC (.addAtom atm))))
        ;add all bonds
        (doseq [bond (.bonds aa)]
          (do
            (doto bond (.setProperty StandardGenerator/HIGHLIGHT_COLOR (nth colors @aacount)))
            (doto AC (.addBond bond))))
        (swap! aacount inc)))
    ;add the peptidebonds
    (doseq [bond bonds]
      (doto AC (.addBond bond)))
    ;add the C-terminus Oxygen
    (let [newO (Atom. "O")
          cbond (Bond. newO (.getCTerminus (last aaseq)))]
      (doto AC (.addAtom newO) (.addBond cbond)))

    AC))


(def default-image-options
  {:width               200
   :height              200
   :font                (Font. "Verdana" Font/PLAIN 18)
   :annotationcolor     Color/RED
   :annotationdistance  0.25
   :annotationfontscale 0.4
   :atomcolor           (UniColor. Color/BLACK)
   :bondseparation      0.18
   :fancyboldwedges     true
   :fancyhashedwedges   true
   :hashspacing         5
   :highlighting        (UniColor. Color/RED)
   :highlightstyle      (. StandardGenerator$HighlightStyle OuterGlow)
   :outerglowwidth      6.0
   :strokeratio         1
   :symbolmarginratio   2
   :visibility          (. SymbolVisibility iupacRecommendationsWithoutTerminalCarbon)
   :wavespacing         5
   :wedgeratio          8})


(defn peptideanimation
  "take a series of aminoacids and generate a GIF of them growing"
  [filename aminos & {:keys [width height delay loops lastdelay]
                      :or   {width 400 height 500 delay 50
                             loops 0 lastdelay 100}}]
  (let [genimage (fn [m] (makeimage m :width width :height height
                                    :highlightstyle (. StandardGenerator$HighlightStyle OuterGlow)))
        peps (map #(take % aminos) (map inc (range (count aminos))))
        peptides (map makepeptide peps)
        images (map genimage peptides)]
    (write-gif filename images :delay delay :loops loops :lastdelay lastdelay)))

(defn makeimage [^IAtomContainer molecule & opts]
  "convert an atomcontainer to a BufferedImage"
  ^BufferedImage
   (let [props (merge default-image-options (apply hash-map opts))
         mol2d (get-2D molecule)
         width (:width props)
         height (:height props)
         gen (doto (ArrayList.)
               (.add (new BasicSceneGenerator))
               (.add (new StandardGenerator (:font props))))
         ;;Setup the Drawing
         image (BufferedImage. width height 1)
         drawArea (Rectangle. width height)
         g (doto (.getGraphics image)
             (.fillRect 0 0 width height))
         renderer (AtomContainerRenderer. gen (new AWTFontManager))
         rendererModel (.getRenderer2DModel renderer)]

     ;add the global changes here
     (doto rendererModel
       (.set StandardGenerator$AnnotationColor (:annotationcolor props))
       (.set StandardGenerator$AnnotationDistance (double (:annotationdistance props)))
       (.set StandardGenerator$AnnotationFontScale (double (:annotationfontscale props)))
       (.set StandardGenerator$AtomColor (:atomcolor props))
       (.set StandardGenerator$BondSeparation (double (:bondseparation props)))
       (.set StandardGenerator$FancyBoldWedges (:fancyboldwedges props))
       (.set StandardGenerator$FancyHashedWedges (:fancyhashedwedges props))
       (.set StandardGenerator$HashSpacing (double (:hashspacing props)))
       (.set StandardGenerator$Highlighting (:highlightstyle props))
       (.set StandardGenerator$OuterGlowWidth (:outerglowwidth props))
       (.set StandardGenerator$StrokeRatio (double (:strokeratio props)))
       (.set StandardGenerator$SymbolMarginRatio (double (:symbolmarginratio props)))
       (.set StandardGenerator$Visibility (:visibility props))
       (.set StandardGenerator$WaveSpacing (double (:wavespacing props)))
       (.set StandardGenerator$WedgeRatio (double (:wedgeratio props))))

     ; render the image
     (-> renderer (. setup mol2d drawArea))
     (-> renderer (. paint mol2d (new AWTDrawVisitor g)))
     image))

(comment
  (def aminos (keys AminoAcids))

  (def pep1 (makepeptide (take 3 aminos)))
  (def image1 (makeimage pep1 :width 800 :height 200 :highlightstyle (. StandardGenerator$HighlightStyle OuterGlow)))
  (show image1)
  (save image1 "peptide-image1.png")
  ; issue with sidechain rotation

  (def pep2 (makepeptide (take 3 (drop 3 aminos))))
  (def image2 (makeimage pep2 :width 800 :height 200 :highlightstyle (. StandardGenerator$HighlightStyle OuterGlow)))
  (show image2)
  (save image2 "peptide-image2.png")
  ;linear and fine

  (def pep3 (makepeptide (take 3 (drop 6 aminos))))
  (def image3 (makeimage pep3 :width 800 :height 200 :highlightstyle (. StandardGenerator$HighlightStyle OuterGlow)))
  (show image3)
  (save image3 "peptide-image3.png")
  ; linear fine

  (def pep4 (makepeptide (take 3 (drop 9 aminos))))
  (def image4 (makeimage pep4 :width 800 :height 200 :highlightstyle (. StandardGenerator$HighlightStyle OuterGlow)))
  (show image4)
  (save image4 "peptide-image4.png")

  ; issue with side chain rotation

  (def pep5 (makepeptide (take 3 (drop 12 aminos))))
  (def image5 (makeimage pep5 :width 800 :height 600 :highlightstyle (. StandardGenerator$HighlightStyle OuterGlow)))
  (show image5)
  (save image5 "peptide-image5.png")

  (def pep17 (makepeptide (take 17 aminos)))
  (def image17 (makeimage pep17 :width 1500 :height 500))
  (show image17)
  (save image17 "/Users/zachpowers/Desktop/peptide-image17.png")

  ; incorrect bond rotation

  (peptideanimation "/Users/zachpowers/Desktop/peptidechain.gif"
                    (take 17 aminos) :width 1000 :height 800))



