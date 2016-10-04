(defproject dfmm "0.1.0-SNAPSHOT"
  :description "Data Flow Matrix Machines"
  :url "https://github.com/anhinga/fluid"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha10"]
                 [net.mikera/vectorz-clj "0.45.0"]
                 [org.clojure/tools.nrepl  "0.2.12"] ; Explicit nREPL

                 [org.clojure/core.async    "0.1.346.0-17112a-alpha"]
                 [org.clojure/data.json     "0.2.6"]

                 [aerial.fs                 "1.1.5"]
                 [aerial.utils              "1.0.6"]
                 [aerial.bio.utils          "1.0.0"]

                 [net.apribase/clj-dns      "0.1.0"] ; reverse-dns-lookup
                 ]

  :plugins [[cider/cider-nrepl "0.12.0"]
            [refactor-nrepl    "2.2.0"]]

  ;;:aot :all

  :repositories
  {"lclrepo" "file:lclrepo"})
