(ns ^:figwheel-always roguelike.core
    (:require [reagent.core :as reagent :refer [atom]]))

(declare world-tick!)

(enable-console-print!)

(defonce enemies      (atom []))
(defonce stages       (atom {}))
(defonce stage-coords (atom {:x 0 :y 0}))
(defonce position     (atom {:x 0
                             :y 0}))

(def unit-size 8)

(defn window-dimensions []
  {:width (.-innerWidth js/window)
   :height (.-innerHeight js/window)})

(defn map-values [f coll]
  (into {} (map (fn [[k v]]
                  [k (f v)])
                coll)))

(defn world-dimensions []
  (map-values #(js/Math.floor (/ % unit-size))
              (window-dimensions)))

(defn generate-random-enemy []
  (let [{:keys [height width]} (window-dimensions)]
    {:x (rand-int (js/Math.floor (/ width  unit-size)))
     :y (rand-int (js/Math.floor (/ height unit-size)))}))

(defn run-out-of-the-world? []
  (let [{:keys [x y]} @position
        {:keys [height width]} (world-dimensions)]
    (cond
      (< x 0) :left
      (< y 0)  :top
      (> x width) :right
      (> y height) :bottom
      :else false)))

(defn reset-to-the-center! []
  (let [{:keys [height width]} (world-dimensions)]
    (reset! position {:y (js/Math.floor (/ height 2))
                      :x (js/Math.floor (/ width 2))})))

(defn jump-to-next-stage! [direction]
  (let [{:keys [height width]} (world-dimensions)
        pos (case direction
              :left {:x (dec width)}
              :top {:y (dec height)}
              :right {:x 0}
              :bottom {:y 0})
        stage-pos (case direction
              :left   (update @stage-coords :y dec)
              :top    (update @stage-coords :x dec)
              :right  (update @stage-coords :y inc)
              :bottom (update @stage-coords :x inc))]
    (swap! position merge pos)
    (reset! stage-coords stage-pos)))

(defn key-down [e]
  (let [[field value] (case (.-keyCode e)
                        37 [:x -1] ;left
                        40 [:y 1]  ;down
                        38 [:y -1] ;up
                        39 [:x 1]  ;right
                        [])]
    (when (and field value)
      (swap! position update field (partial + value))
      (world-tick!))
    (if-let [direction (run-out-of-the-world?)]
      (jump-to-next-stage! direction)
      ; (reset! enemies [(generate-random-enemy)])
      )))

(defn bind-events []
  (set! (.-onkeydown js/document)
        key-down))

(defonce key-bind
  (bind-events))

(defn move-enemy [{player-x :x player-y :y :as player}
                  {enemy-x :x enemy-y :y :as enemy}]
  (let [x-diff (- player-x enemy-x)
        y-diff (- player-y enemy-y)]
    (if (> (js/Math.abs x-diff)
           (js/Math.abs y-diff))
      (update enemy :x (if (pos? x-diff) inc dec))
      (update enemy :y (if (pos? y-diff) inc dec)))))

(defn world-tick! []
  (swap! enemies (partial map (partial move-enemy @position)))
  (prn :enemies @enemies))

(defn enemy-component [i {:keys [x y]}]
  ^{:key (str "enemy" i)}
  [:div
   {:style {:position :absolute
            :top (* y unit-size)
            :left (* x unit-size)
            :height unit-size
            :width unit-size
            :border "1px solid red"
            :overflow :hidden
            :background-color :red}}])

(defn player-component []
  (let [{:keys [x y]} @position]
    [:div#player
     {:style {:position :absolute
              :top (* y unit-size)
              :left (* x unit-size)
              :height unit-size
              :width unit-size
              :border "1px solid black"
              :overflow :hidden
              :background-color :black}}]))

(defn root []
  ; (prn :position @position)
  ; (prn :stage-coords @stage-coords)
  [:div
   (map-indexed enemy-component @enemies)
   [player-component]])

(defn -main []
  (reset-to-the-center!)
  (reset! enemies (repeatedly (rand-int 15) generate-random-enemy))
  (reagent/render-component [root] (.-body js/document)))

(-main)
