(ns leiningen.cloverage
  (:require [leiningen.run :as run]
            [bultitude.core :as blt]))

(defn ns-names-for-dirs [dirs]
  (map name (mapcat blt/namespaces-in-dir dirs)))

(defn get-lib-version []
  (or (System/getenv "CLOVERAGE_VERSION") "1.0.5-SNAPSHOT"))

(defn project-file-to-commandline-args [data]
  (reduce (fn [r [k v]]
            (if (sequential? v)
              (reduce (fn [l val] (conj l (str "--" (name k)) val)) [] v)
              (concat r (list (str "--" (name k)) v))))
          '()
          data))

(defn cloverage
  "Run code coverage on the project.

  To specify cloverage version, set the CLOVERAGE_VERSION environment variable.
  Specify -o OUTPUTDIR for output directory, for other options see cloverage."
  [project & args]
  (let [source-namespaces (ns-names-for-dirs (:source-paths project))
        test-namespace    (ns-names-for-dirs (:test-paths project))
        cloverage-args    (project-file-to-commandline-args (:cloverage project))]

    (apply run/run (update-in project [:dependencies]
                              conj    ['cloverage (get-lib-version)])
           "-m" "cloverage.coverage"
           (concat (mapcat  #(list "-x" %) test-namespace)
                   args
                   cloverage-args
                   source-namespaces))))
