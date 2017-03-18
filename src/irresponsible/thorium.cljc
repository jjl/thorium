(ns irresponsible.thorium
  (:require [#?(:clj clojure.spec :cljs cljs.spec) :as s]))

(defonce ^{:dynamic true
           :doc "A compile-time flag that controls whether probes are compiled into code using them"}
  *compile-traces*
  #?(:clj  (not= "false" (System/getProperty "irresponsible.thorium.compile-traces"))
     :cljs true))

(defonce ^{:dynamic true
           :doc "Holds a volatile map of traces when in the dynamic scope fo a trap"}
  *trace*
  nil)

(defmacro trap
  "Instruments the dynamic scope of the body expressions so that probe calls
   which were compiled while *compile-traces* was true will collect the probe
   values into a map which will be the return value of trap
   args: [& exprs]
   returns: code"
  [& exprs]
  `(binding [*trace* (volatile! {})]
     ~@exprs
     @*trace*))

(defmacro probe
  "Stores the result of calling fn into the probe trace under key
   args: [key fn & args]
     key: keyword name in the map to store under
     fn: function of [trace-map key & args] -> trace-map
     args: args to apply to fn
   returns: code
  "
  [key f & args]
  (when *compile-traces*
    `(when *trace*
      (vswap! *trace* ~f ~key ~@args))))

(defmacro ?!
  [key f & args]
  `(probe ~key update ~f ~@args))
  
(s/fdef ?!
   :args (s/cat :key keyword? :fn any? :args (s/* any?)))

(defmacro ?=
  "Adds an instrumentation probe with the given name for the given expression
   In order to work, probes must be:
     - compiled while *compile-traces* is true
     - run within the dynamic scope of a trap block
   args: [name expr]
   returns: code"
  [key expr]
  (if *compile-traces*
    `(let [e# ~expr]
       (probe ~key (fn [m# k#] (assoc m# k# e#)))
       e#)
    expr))

(s/fdef ?=
   :args (s/cat :name keyword? :expr any?))        

(defmacro ??=
  "backwards of `?=`
   args: [expr key]
   returns: code"
  [expr key]
  `(?= ~key ~expr))

(s/fdef ??=
   :args (s/cat :expr any? :name keyword?))

(defmacro ?+
  ""
  [key]
  `(?! ~key (fnil inc 0)))

;; (macroexpand '(?+ :abc))
