# drawpeptides

Description and motivation can be found at this [blog post](http://localhost:1313/blog/2015/clojure-cdk-peptides/).

## Installation

```
git clone https://github.com/zachcp/drawpeptides.git
cd drawpeptides
lein repl
```

## Usage

The core functions are `makepeptide`, `makeimage` and `peptideanimation`. `makepeptide` takes a vector of keywords and
turns it into a peptide. `makeimage` creaets and image and can take some keyword parameters
 to change the size and the CDK options. `peptideanimation` takes a filename and vector of images to create an animatedgif.


```[clojure]

    (def aminos (keys AminoAcids))
    ;make a peptide
    (def pep1 (makepeptide (take 3 aminos)))
    ; make an image
    (def image1 (makeimage pep1 :width 800 :height 200  :highlightstyle (. StandardGenerator$HighlightStyle OuterGlow)))
    ;show it
    (show image1)

    (show
      (makeimage
        (makepeptide [:ALA :ALA :ALA :ALA])))

    ;To generate an animated peptide and write it to the file
    (peptideanimation "/Users/yourusername/Desktop/simplepeptide.gif"
                       [:PHE :ALA :ASP :GLY] :width 400 :height 800)
```

![animatespeptide](https://raw.githubusercontent.com/zachcp/drawpeptides/master/resources/simplepeptide.gif)



## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
