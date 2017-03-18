(ns irresponsible.thorium-test
  (:require [#?(:clj clojure.test :cljs cljs.test) :as t]
            [irresponsible.thorium :as th #?@(:cljs [:include-macros true])]))

#?
(:clj
 (t/deftest disabled
   (binding [th/*compile-traces* false]
     (t/is (= {}
              (eval `(th/trap
                      (th/?= :abc 123)
                      (th/?= :abc 456)
                      (th/??= 123 :def)
                      (th/?+ :def)
                      (th/?+ :ghi))))))))

(t/deftest enabled
  (t/is (= {:abc 456 :def 124 :ghi 1}
           (th/trap
            (th/?= :abc 123)
            (th/?= :abc 456)
            (th/??= 123 :def)
            (th/?+ :def)
            (th/?+ :ghi)))))
