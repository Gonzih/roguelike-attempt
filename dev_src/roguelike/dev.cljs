(ns roguelike.dev
    (:require
     [roguelike.core]
     [figwheel.client :as fw]))

(fw/start {
  :on-jsload (fn []
               ; (roguelike.core.-main)
               )})
