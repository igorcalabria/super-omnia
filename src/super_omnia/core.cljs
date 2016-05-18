(ns super-omnia.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch-sync]]
            [super-omnia.handlers]
            [super-omnia.subs]
            [super-omnia.views :refer[app]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defn mount-root []
  (reagent/render-component [app]
                            (. js/document (getElementById "app"))))
(defn ^:export main []
  (dispatch-sync [:initialise-db])
  (mount-root)
  )

(defn on-js-reload []
  (mount-root))
