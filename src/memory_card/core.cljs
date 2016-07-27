(ns memory-card.core
  (:require [rum.core :as rum]
            [clojure.string :refer [replace capitalize]]
            [goog.string :as gstring]))

(defn suit [s]
  (let [suits {:heart "&#9829;"
               :diamond "&#9830;"
               :spade "&#9824;"
               :club "&#9827;"}]
    (gstring/unescapeEntities (suits s))))

(defn rankify [r]
  (let [royalty #{:king :queen :jack :ace}]
    (if (contains? royalty r)
      (-> r name first capitalize)
      (name r))))

(defn kw->class [k]
  (-> k name (replace #"-" "_")))

(rum/defc label [text]
  [:div {:class "label"} text])

(rum/defc pcf-placement [l s]
  [:span {:class ["suit" (kw->class l)]} (suit s)])

(rum/defc playing-card-face [r s]
  (pcf-placement :middle-center s))

(rum/defc playing-card [r s]
  [:div {:class ["card" (name s) (name r)]}
   [:div {:class ["corner" "top"]}
    [:span {:class "number"} (rankify r)]
    [:span (suit s)]]
   (playing-card-face r s)
   [:div {:class ["corner" "bottom"]}
    [:span {:class "number"} (rankify r)]
    [:span (suit s)]]])

(rum/defc flip-card []
  [:div {:class "flip-container"}
   [:div {:class "flipper"}
    [:div {:class "front"}
     [:div {:class ["card"]}]]
    [:div {:class "back"}
     (playing-card :queen :heart)]]])

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "good Murk."}))

(defn on-js-reload []
  (rum/mount (flip-card) (.getElementById js/document "app"))
  
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
