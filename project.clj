(defproject drawpeptides "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha3"]
                 [net.mikera/imagez "0.10.0"]
                 [org.openscience.cdk/cdk-depict "1.5.13"]
                 [org.openscience.cdk/cdk-bundle "1.5.13"]
                 [gif-clj "1.0.3"]]
  :main ^:skip-aot drawpeptides.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
