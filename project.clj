(defproject drawpeptides "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [net.mikera/imagez "0.5.0"]
                 [org.openscience.cdk/cdk-bundle "1.5.10"]
                 [gif-clj "1.0.3"]]
  :main ^:skip-aot drawpeptides.core
  :repositories [["ebi-repo" "http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
