(ns make
  (:refer-clojure :exclude [test])
  (:require
   [babashka.deps :as deps]
   [babashka.fs :as fs]
   [babashka.process :as p]))

(def lsp-bin (if (#'fs/windows?)
               "clojure-lsp.bat"
               "clojure-lsp"))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn clean
  "Clean all artifacts produced by the various tasks."
  []
  (let [files (into ["cli/target"
                     (fs/path "cli" lsp-bin)
                     "cli/clojure-lsp-standalone.jar"
                     "lib/clojure-lsp.jar"
                     lsp-bin
                     "docs/README.md"
                     "docs/CHANGELOG.md"]
                    (fs/match "." "regex:clojure-lsp.*\\.jar"))]
    (doseq [f files]
      (fs/delete-tree f))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn lib-pom []
  (-> (deps/clojure ["-T:build" "pom"] {:dir "lib" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn cli-pom []
  (-> (deps/clojure ["-T:build" "pom"] {:dir "cli" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn lib-jar []
  (-> (deps/clojure ["-T:build" "jar"] {:dir "lib" :inherit true})
      (p/check))
  (fs/move "lib/target/clojure-lsp.jar" "." {:replace-existing true}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn cli-jar
  "Build `cli` jar."
  []
  (-> (deps/clojure ["-T:build" "prod-jar"] {:dir "cli" :inherit true})
      (p/check))
  (fs/move "cli/target/clojure-lsp-standalone.jar" "." {:replace-existing true}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn cli-debug-jar []
  (-> (deps/clojure ["-T:build" "debug-jar"] {:dir "cli" :inherit true})
      (p/check))
  (fs/move "cli/target/clojure-lsp-standalone.jar" "." {:replace-existing true}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn cli-jar-for-native []
  (-> (deps/clojure ["-T:build" "prod-jar-for-native"]
                    {:dir "cli"})
      (p/check))
  (fs/move "cli/target/clojure-lsp-standalone.jar" "." {:replace-existing true}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn debug-cli
  "Build the `clojure-lsp[.bat]` cli exec script (`cider-nrepl`/`clj-async-profile` support)."
  []
  (-> (deps/clojure ["-T:build" "debug-cli"] {:dir "cli" :inherit true})
      p/check)
  (fs/move (fs/path "cli" lsp-bin) "." {:replace-existing true}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn prod-cli
  "Build the `clojure-lsp[.bat]` cli exec script."
  []
  (-> (deps/clojure ["-T:build" "prod-cli"] {:dir "cli" :inherit true})
      p/check)
  (fs/move (fs/path "cli" lsp-bin) "." {:replace-existing true}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn native-cli
  "Build the native `clojure-lsp[.exe]` cli executable with `graalvm`."
  []
  (-> (deps/clojure ["-T:build" "native-cli"] {:dir "cli" :inherit true})
      (p/check))
  (fs/move (fs/path "cli" lsp-bin) "." {:replace-existing true}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn test-lib
  "Run all unit tests in lib/."
  []
  (println :running-unit-tests... "lib")
  (-> (deps/clojure ["-M:test"] {:dir "lib" :inherit true})
      (p/check))
  (println))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn test-cli
  "Run all unit tests in cli/."
  []
  (println :running-unit-tests... "cli")
  (-> (deps/clojure ["-M:test"] {:dir "cli" :inherit true})
      (p/check))
  (println))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn pod-test []
  (-> (deps/clojure ["-M:pod-test"] {:dir "cli" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn integration-test
  "Run the integration tests in 'test/integration-test/' using `./clojure-lsp[.bat|.exe]`."
  []
  (let [bb (str \" (.get (.command (.info (java.lang.ProcessHandle/current)))) \")]
    (p/shell {:dir "cli"} (str bb " integration-test ../" lsp-bin))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn lint-clean []
  (-> (deps/clojure ["-M:run" "clean-ns" "--dry" "--ns-exclude-regex" "sample-test.*" "--project-root" "../"]
                    {:dir "cli" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn lint-format []
  (-> (deps/clojure ["-M:run" "format" "--dry" "--ns-exclude-regex" "sample-test.*" "--project-root" "../"]
                    {:dir "cli" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn lint-diagnostics []
  (-> (deps/clojure ["-M:run" "diagnostics" "--dry" "--ns-exclude-regex" "sample-test.*" "--project-root" "../"]
                    {:dir "cli" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn lint-fix []
  (doseq [linter ["clean-ns" "format"]]
    (-> (deps/clojure ["-M:run" linter "--ns-exclude-regex" "sample-test.*" "--project-root" "../"]
                      {:dir "cli" :inherit true})
        (p/check))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn release []
  (p/shell "./release"))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn deploy-clojars []
  (-> (deps/clojure ["-T:build" "deploy-clojars"]
                    {:dir "lib" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn deploy-clojars-standalone []
  (-> (deps/clojure ["-T:build" "deploy-clojars"]
                    {:dir "cli" :inherit true})
      (p/check)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn local-webpage []
  (let [files ["CHANGELOG.md" "README.md"]]
    (doseq [f files]
      (fs/copy f "docs" {:replace-existing true}))
    (fs/copy-tree "images" "docs" {:replace-existing true})
    (p/shell "docker login docker.pkg.github.com")
    (p/shell (str "docker run --rm -it -p 8000:8000 -v " (fs/cwd) ":/docs docker.pkg.github.com/clojure-lsp/docs-image/docs-image"))))
