(ns ^:figwheel-always roguelike.core
    (:require [reagent.core :as reagent :refer [atom]]))

(declare world-tick!)

(enable-console-print!)

(defonce stages       (atom {}))
(defonce stage-coords (atom {:x 0 :y 0}))
(defonce position     (atom {:left 0
                             :top 0}))

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

(defn run-out-of-the-world? []
  (let [{:keys [left top]} @position
        {:keys [height width]} (world-dimensions)]
    (cond
      (< left 0) :left
      (< top 0)  :top
      (> left width) :right
      (> top height) :bottom
      :else false)))

(defn reset-to-the-center! []
  (let [{:keys [left top]} @position
        {:keys [height width]} (world-dimensions)]
    (reset! position {:top  (js/Math.floor (/ height 2))
                      :left (js/Math.floor (/ width 2))})))

(defn jump-to-next-stage! [direction]
  (let [{:keys [height width]} (world-dimensions)
        pos (case direction
              :left {:left (dec width)}
              :top {:top (dec height)}
              :right {:left 0}
              :bottom {:top 0})
        stage-pos (case direction
              :left   (update @stage-coords :y dec)
              :top    (update @stage-coords :x dec)
              :right  (update @stage-coords :y inc)
              :bottom (update @stage-coords :x inc))]
    (swap! position merge pos)
    (reset! stage-coords stage-pos)))

(defn key-down [e]
  (let [x :left
        y :top
        [field value] (case (.-keyCode e)
                        37 [x -1] ;left
                        40 [y 1]  ;down
                        38 [y -1] ;up
                        39 [x 1]  ;right
                        [])]
    (when (and field value)
      (swap! position update field (partial + value))
      (world-tick!))
    (if-let [direction (run-out-of-the-world?)]
      (jump-to-next-stage! direction))))

(defn bind-events []
  (set! (.-onkeydown js/document)
        key-down))

(defonce key-bind
  (bind-events))

(defn world-tick! [])

(defn root []
  (let [{:keys [left top]} @position]
    (prn @position)
    (prn @stage-coords)
    [:div
     {:style {:position :absolute
              :top (* top unit-size)
              :left (* left unit-size)
              :height unit-size
              :width unit-size
              :border "1px solid black"
              :overflow :hidden
              :background-color :black}}]))

(defn -main []
  (reset-to-the-center!)
  (reagent/render-component [root] (.-body js/document)))

(-main)
