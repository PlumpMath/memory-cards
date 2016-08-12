(ns memory-card.mahjong
  (:require [clojure.string :refer [replace capitalize]]))

(def tiles
  {:east-wind "🀀"  
   :south-wind "🀁"  
   :west-wind "🀂"  
   :north-wind "🀃"  
   :red-dragon "🀄"  
   :green-dragon "🀅"  
   :white-dragon "🀆"  
   :one-of-characters "🀇"  
   :two-of-characters "🀈"  
   :three-of-characters "🀉"  
   :four-of-characters "🀊"  
   :five-of-characters "🀋"  
   :six-of-characters "🀌"  
   :seven-of-characters "🀍"  
   :eight-of-characters "🀎"  
   :nine-of-characters "🀏"  
   :one-of-bamboos "🀐"  
   :two-of-bamboos "🀑"  
   :three-of-bamboos "🀒"  
   :four-of-bamboos "🀓"  
   :five-of-bamboos "🀔"  
   :six-of-bamboos "🀕"  
   :seven-of-bamboos "🀖"  
   :eight-of-bamboos "🀗"  
   :nine-of-bamboos "🀘"  
   :one-of-circles "🀙"  
   :two-of-circles "🀚"  
   :three-of-circles "🀛"  
   :four-of-circles "🀜"  
   :five-of-circles "🀝"  
   :six-of-circles "🀞"  
   :seven-of-circles "🀟"  
   :eight-of-circles "🀠"  
   :nine-of-circles "🀡"  
   :plum "🀢"  
   :orchid "🀣"  
   :bamboo "🀤"  
   :chrysanthemum "🀥"  
   :spring "🀦"
   :summer "🀧"
   :autumn "🀨"
   :winter "🀩"
   :joker "🀪"
   :back "🀫"})


(defn tile [s]
  [:div {:class ["card" (name s)]}
   [:div {;:class ["suit" "middle_center"]
           :style #js {:font-size "24em"
                       :width "100%"
                       :height "100%"}} (tiles s)]])
