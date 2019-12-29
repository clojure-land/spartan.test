(ns spartan.test
  "A light test framework compatible with babashka."
  (:require [spartan.impl.test :as impl]))

(defmacro is [& body]
  `(try
     (let [res# (do ~@body)]
       (if res#
         (swap! impl/report-counter update :success conj impl/*current-test*)
         (do (binding [*out* *err*]
               (println (format "FAIL in %s. Expected %s but got %s." impl/*current-test* (str '~@body) res#))
               (swap! impl/report-counter update :fail conj impl/*current-test*)))))
     (catch java.lang.Exception e#
       (binding [*out* *err*]
         (println (format "ERROR in %s. Expected %s but got %s" impl/*current-test* (str '~@body) e#))
         (swap! impl/report-counter update :error conj impl/*current-test*)))))

(defmacro deftest [symbol & body]
  `(let [sym# (symbol (str (ns-name *ns*))
                      (str '~symbol))]
     (defn ~symbol []
       (binding [impl/*current-test* sym#]
         ~@body))
       (swap! impl/registered-tests conj sym#)))

(defn -main [& args]
  (let [{:keys [:namespaces :tests] :as _parsed} (impl/parse-args args)]
    (doseq [n namespaces]
      (require n))
    (doseq [v tests]
      (require (symbol (namespace v))))
    (let [{:keys [:error :fail]} (impl/run-tests tests)]
      (System/exit (+ error fail)))))

;;;; Scratch
