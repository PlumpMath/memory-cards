(ns memory-card.core
  (:require [rum.core :as rum]))

(rum/defc label [text]
  [:div {:class "label"} text])

(enable-console-print!)

(println "This text is printed from src/memory-card/core.cljs. Go ahead and edit it and see reloading in action....")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "good Murk."}))


(defn on-js-reload []
  (rum/mount (label (:text @app-state)) (.getElementById js/document "app"))
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
