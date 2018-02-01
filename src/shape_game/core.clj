(ns shape-game.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def width 800)
(def height 600)
(def background-color 240)
(def fps 60)

(def rect-width (/ width 4))
(def rect-height (/ height 20))
(def rect-x-init (/ (- width rect-width) 2))
(def rect-y-init (- height rect-height 10))
(def rect-x-step 4)
(def rect-x-speed-init 0)
(def rect-dir :none)

(def ellipse-wh (/ rect-width 10))
(def ellipse-x-init (+ rect-x-init (/ rect-width 2)))
(def ellipse-y-init (- rect-y-init (/ ellipse-wh 2)) )
(def ellipse-y-step 4)
(def ellipse-x-speed-init 0)
(def ellipse-sign-y (atom -))

(defn setup []
  (q/frame-rate fps)
  (q/color-mode :hsb)
  {:rect-x rect-x-init
   :rect-x-speed rect-x-speed-init
   :rect-y rect-y-init
   :rect-dir rect-dir
   :ellipse-x ellipse-x-init
   :ellipse-x-speed ellipse-x-speed-init
   :ellipse-sign-x +
   :ellipse-sign-y -
   :ellipse-y ellipse-y-init})

(defn update-state [state]
  (->
   (cond
    (= (:ellipse-y state) (/ ellipse-wh 2))
    (do
      (->
       (update state :rect-x-speed (fn [_] 2))
       (update :ellipse-sign-y (fn [_] +))))
    (= (:ellipse-y state) (+ (- rect-y-init rect-height) ellipse-wh))
    (do
      (->
       (update state :ellipse-x-speed (fn [_] (:rect-x-speed state)))
       (update :ellipse-sign-y (fn [_] -))
       (update :ellipse-sign-x (fn [y] (if (= :left (:rect-dir state)) + -)))))
    (= (:ellipse-x state) (- width ellipse-wh))
    (->
     (update state :ellipse-sign-x (fn [_] -))
     (update :ellipse-x (fn [x] ((:ellipse-sign-x state) x (:ellipse-x-speed state)))))
    :else
    (update state :ellipse-x (fn [x] ((:ellipse-sign-x state) x (:ellipse-x-speed state)))))
   (update :ellipse-y (fn [y] ((:ellipse-sign-y state) y ellipse-y-step)))))

(defn draw-state [state]
  (q/background background-color)
  (q/ellipse (:ellipse-x state) (:ellipse-y state) ellipse-wh ellipse-wh)
  (q/rect (:rect-x state) (:rect-y state) rect-width rect-height))

(q/defsketch shape-game
  :title "Shape game"
  :size [width height]
  :setup setup
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  :middleware [m/fun-mode]
  :key-pressed (fn [{:keys [rect-x] :as state} { :keys [key key-code] }]
                 (->
                  (case key
                   (:right)
                   (update state :rect-x (partial + rect-x-step))
                   (:left)
                   (update state :rect-x (fn [x] (- x rect-x-step)))
                   state)
                  (update :rect-dir (fn [_] key)))))
