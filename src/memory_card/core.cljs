(ns memory-card.core
  (:require [rum.core :as rum]
            [clojure.string :refer [replace capitalize]]
            [goog.string :as gstring]))

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
  [:div {:class "label"} text])

(rum/defc pcf-placement [l s]
  [:span {:class ["suit" (kw->class l)]} (suit s)])

(rum/defc playing-card-face [r s]
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

(rum/defc playing-card [r s]
  [:div {:class ["card" (name s) (name r)]}
   [:div {:class ["corner" "top"]}
    [:span {:class "number"} (rankify r)]
    [:span (suit s)]]
   (playing-card-face r s)
   [:div {:class ["corner" "bottom"]}
    [:span {:class "number"} (rankify r)]
    [:span (suit s)]]])

(rum/defcs flip-card < (rum/local false ::george)
  [state back]
  (let [local-atom (::george state)]
    [:div {:class ["flip-container" (when @local-atom "hover")]
          :on-click (fn [_] (swap! local-atom not))}
    [:div {:class "flipper"}
     [:div {:class "front"}
      [:div {:class "card"}]]
     [:div {:class "back"} back]]]))

(rum/defc field []
  (let [crds (for [s (mapv first suits)
                   r (->> (range 1 11) reverse (map str)
                          (map keyword)
                          (into [:king :queen :jack :ace]))]
            (flip-card (playing-card r s)))]
    (apply (partial vector :div {:id "table"}) crds)))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:flipped [[]]}))

(defn on-js-reload []
  (rum/mount (field) (.getElementById js/document "app"))
  
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
