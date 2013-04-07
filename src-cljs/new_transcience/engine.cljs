(ns new-transcience.engine
  (:use [jayq.core :only [$ append]]))

(def stage
  (createjs/Stage. "demoCanvas"))

;;(.addChild stage circle)

;; (.update stage)


(defn update-screen-60hz []
  (js/setInterval #(.update stage) 15))

;;(js/clearInterval screen-refresh)
(def screen-refresh (update-screen-60hz))


(defn approach-point [x target]
  (cond 
    (< x target) (inc x)
    (> x target) (dec x)
    :else x))

;; registers a shape to update when it's atom changes
(defn register-shape-atom [shape]
  ;; Add a watch on this atom to update the screen
  (add-watch shape :movements
             (fn [k r old-state new-state]
               (let [shape (:easel-shape new-state)]
                 (set! (.-x shape) (:x new-state))
                 (set! (.-y shape) (:y new-state)))))
  shape)

(defn add-and-update-stage [shape] 
  (.addChild stage shape)
  (.update stage))

;; Returns an atom containing the state of that square
(defn create-square [{:keys [x y w h color] :or {x 0 y 0 w 10 h 10 color "blue"}}]
  (let [square (createjs/Shape.)]
    (-> (.-graphics square)
      (.beginFill color)
      (.drawRect x y w h))
    (add-and-update-stage square)
    (register-shape-atom
      (atom {:easel-shape square
             :x x
             :h h
             :w w
             :y y}))))

(defn create-circle [{:keys [x y r color] :or {x 0 y 0 r 10 color "red"}}]
  (let [circle (createjs/Shape.)]
    (-> 
        (.-graphics circle)
        (.beginFill color)
        (.drawCircle x y r))
    (add-and-update-stage circle)
    (register-shape-atom 
      (atom {:easel-shape circle
             :x x
             :r r
             :y y}))))

(defn destroy-shape [shape]
  (.removeChild stage (:easel-shape shape))
  (.update stage))


(defn move-item-to [entity x y speed]
  (js/clearTimeout (:movement @entity 0))
  (when (or 
          (not= x (:x @entity)) 
          (not= y (:y @entity))) 
    (swap! entity #(assoc % :x (approach-point (:x @entity) x)
                            :y (approach-point (:y @entity) y)))
    (swap! entity 
           (fn [e]
             (assoc e
                   :movement
                   (js/setTimeout #(move-item-to entity x y speed) (/ 1000 speed)))))))


(.log js/console "now we are reqady for some real development!")