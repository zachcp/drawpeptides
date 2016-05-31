(ns drawpeptides.data
  (:require [drawpeptides.core :as c])
  (:import [org.openscience.cdk.io CMLWriter CMLReader])
  (:import [org.openscience.cdk ChemFile])
  (:import [org.openscience.cdk.tools.manipulator ChemFileManipulator])
  (:import [org.openscience.cdk.libio.cml ICMLCustomizer])
  (:import [java.io FileWriter FileReader FileInputStream])
  (:import [org.openscience.cdk AtomContainer]))


(defn writeCML
  "write a cmlfile given an AtomContainer and a name"
  [^AtomContainer container ^String name]
  (let [w (FileWriter. name)
        cmlw (CMLWriter. w)]
    (doto cmlw (.write container) (.close))))


(defn readCML
  "Reads a CML file and returns a molecule"
  [filename]
  (let [reader (CMLReader. (FileInputStream. filename))
        cf (.read reader (ChemFile.))]
    (first (. ChemFileManipulator getAllAtomContainers cf))))


(defn convertsmiles []
  "convert smiles to cml"
  (for [[k smiles] c/AminoAcids]
    (let [aa (name k)
          mol (-> smiles (c/parsesmiles) (c/get-2D))
          aapath (str "data/" aa ".cml")]
      (writeCML mol aapath))))


(convertsmiles)

(let [[k smiles] (first c/AminoAcids)
      mol (-> smiles (c/parsesmiles) (c/get-2D))]

  (println mol)
  (for [atm (.atoms mol)]
    (do (println atm)
        (println (.getElem)))))

;StringWriter output = new StringWriter();
;boolean makeFragment = true;
;CMLWriter cmlwriter = new CMLWriter(output, makeFragment);
;cmlwriter.write(molecule);
;cmlwriter.close();
;String cmlcode = output.toString();
;
;Output to a file called "molecule.cml" can done with:
;
;FileWriter output = new FileWriter("molecule.cml");
;CMLWriter cmlwriter = new CMLWriter(output);
;cmlwriter.write(molecule);
;cmlwriter.close();