(ns memory-card.core
  (:require [rum.core :as rum]
            [cljs-uuid-utils.core :as uuid]
            [clojure.string :refer [replace capitalize]]
            [goog.crypt :as gcrypt]
            [goog.crypt.Sha1 :as Sha1]
            [goog.string :as gstring]))


(defn string->bytes [s]
  (gcrypt/stringToUtf8ByteArray s))  ;; must be utf8 byte array

(defn bytes->hex
  "convert bytes to hex"
  [bytes-in]
  (gcrypt/byteArrayToHex bytes-in))

(defn hash-bytes [digester bytes-in]
  (do
    (.update digester bytes-in)
    (.digest digester)))

(defn sha1-
  "convert bytes to sha1 bytes"
  [bytes-in]
  (hash-bytes (goog.crypt.Sha1.) bytes-in))

(defn sha1-bytes
  "convert utf8 string to md5 byte array"
  [string]
  (sha1- (string->bytes string)))

(defn sha1-hex [string]
  "convert utf8 string to sha1 hex string"
  (bytes->hex (sha1-bytes string)))


;; define your app data so that it doesn't get over-written on reload
;; [:r :s]
(defonce app-state (atom {:flipped #{}
                          :matched #{}
                          :turns [0]}))

(def suits {:heart "&#9829;"
            :diamond "&#9830;"
            :spade "&#9824;"
            :club "&#9827;"})


(defn suit [s]
  (gstring/unescapeEntities (suits s)))

(defn rankify [r]
  (let [royalty #{:king :queen :jack :ace}]
    (if (contains? royalty r)
      (-> r name first capitalize)
      (name r))))

(defn kw->class [k]
  (-> k name (replace #"-" "_")))

(rum/defc label [text]
  [:div.label text])

(rum/defc pcf-placement [l s]
  [:span {:class ["suit" (kw->class l)]} (suit s)])

(defn playing-card-face [r s]
  (let [r1 [(pcf-placement :middle-center s)]
        r2 [(pcf-placement :top-center s)
             (pcf-placement :bottom-center s)]
        r4 [(pcf-placement :top-left s)
            (pcf-placement :top-right s)
            (pcf-placement :bottom-left s)
            (pcf-placement :bottom-right s)]
        rm2 [(pcf-placement :middle-left s)
             (pcf-placement :middle-right s)]
        r6 (conj r4 rm2)
        r7 (conj r6 (pcf-placement :middle-top s))
        r8 (conj r7 (pcf-placement :middle-bottom s))
        r9 (conj r4 r1
                 (pcf-placement :middle-top-left s)
                 (pcf-placement :middle-top-right s)
                 (pcf-placement :middle-bottom-left s)
                 (pcf-placement :middle-bottom-right s))
        r10 (conj r4
                  (pcf-placement :middle-top s)
                  (pcf-placement :middle-bottom s)
                  (pcf-placement :middle-top-left s)
                  (pcf-placement :middle-top-right s)
                  (pcf-placement :middle-bottom-left s)
                  (pcf-placement :middle-bottom-right s))
        m {:1 r1 :2 r2 :3 (conj r1 r2) :4 r4 :5 (conj r1 r4)
           :6 r6 :7 r7 :8 r8 :9 r9 :10 r10
           :queen r1 :king r1 :ace r1 :jack r1}]
    (apply (partial vector :div) (get m r))))

(defn playing-card [r s]
  [:div {:class ["card" (name s) (name r)]}
   [:div {:class ["corner" "top"]}
    [:span.number (rankify r)]
    [:span (suit s)]]
   (playing-card-face r s)
   [:div {:class ["corner" "bottom"]}
    [:span.number (rankify r)]
    [:span (suit s)]]])

(rum/defc flip-card < rum/reactive
  [r s n]
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
                                 (js/setTimeout ;;#(clicky r s sha-1)
                                  #(when (> (count (:flipped @app-state)) 2)
                                     (swap! app-state update-in [:flipped] empty))
                                  1500))))}
     [:div.flipper
      [:div.front
       [:div.card]]
      [:div.back (playing-card r s)]]]))

(defn whole-card-deck []
  (for [s (mapv first suits)
        r (->> (range 1 11) reverse (map str)
               (map keyword)
               (into [:king :queen :jack :ace]))]
    [s r]))

(rum/defc whole-deck []
  (apply (partial vector :div#cards) (whole-card-deck)))


(rum/defc play-field [size]
  (let [d-count (/ size 2)
        deck (whole-card-deck)
        fln (comp vec flatten)
        match-deck (->> deck
                        shuffle
                        (take d-count))
        ddd (shuffle (interleave match-deck match-deck))
        md-o (map-indexed (fn [n [s r]]
                            (flip-card r s n))
                          ddd)]
    [:div#cards md-o]))

(enable-console-print!)


(defn restart-play []
  (reset! app-state {:flipped #{} :matched #{} :turns [0]})
  (rum/mount (play-field 20) (.getElementById js/document "table")))

(defn on-js-reload []
  ;; (rum/mount (whole-deck) (.getElementById js/document "table"))

  (restart-play)
  
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

(restart-play)
