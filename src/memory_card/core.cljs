(ns memory-card.core
  (:require [rum.core :as rum]
            [memory-card.crypto :refer [sha1-hex]]
            [memory-card.mahjong :refer [tiles tile]]
            [memory-card.playing-cards :refer [suits playing-card whole-card-deck]]))

;; define your app data so that it doesn't get over-written on reload
;; [:r :s]
(defonce app-state (atom {:flipped #{}
                          :matched #{}
                          :timer nil}))

(rum/defc flip-card < rum/reactive
  [s r n]
  (let [sha-1 (sha1-hex (str r s n))]
    [:div {:key sha-1
           :class ["flip-container" (let [fl (rum/react app-state)]
                                      (when (or (contains? (:flipped fl) sha-1)
                                                (contains? (:matched fl) sha-1))
                                        "hover"))]
           :on-click (fn [_]
                       (cond
                         (and (not (contains? (:flipped @app-state) sha-1))
                              (contains? (:flipped @app-state) [r s]))
                         (do
                           (swap! app-state update-in [:flipped] disj [r s])
                           (let [m-c (first (:flipped @app-state))]
                             (swap! app-state update-in [:flipped] empty)
                             (swap! app-state update-in [:matched] merge sha-1)
                             (swap! app-state update-in [:matched] merge m-c)))
                         :else (do
                                 (swap! app-state update-in [:flipped] merge sha-1)
                                 (swap! app-state update-in [:flipped] merge [r s])
                                 ;; this prevents an old timeout hanging around causing cards to flip too soon are when unexpected
                                 (js/clearTimeout (:timer @app-state))
                                 (swap! app-state assoc :timer
                                        (js/setTimeout
                                         #(when (> (count (:flipped @app-state)) 2)
                                            (swap! app-state update-in [:flipped] empty))
                                         1500)))))}
     [:div.flipper
      [:div.front
       [:div.card]]
      [:div.back (tile s)]]]))

(rum/defc play-field [size]
  (let [d-count (/ size 2)
        deck (map vector (keys tiles))
        ;; deck (whole-card-deck)
        fln (comp vec flatten)
        match-deck (->> deck
                        shuffle
                        (take d-count))
        ddd (shuffle (interleave match-deck match-deck))
        md-o (map-indexed (fn [n [s r]]
                            (flip-card s r n))
                          ddd)]
    [:div#cards md-o]))

(enable-console-print!)

(defn restart-play []
  (reset! app-state {:flipped #{} :matched #{} :timer nil})
  (rum/mount (play-field 20) (.getElementById js/document "table")))

(defn on-js-reload []
  (restart-play)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

(restart-play)

