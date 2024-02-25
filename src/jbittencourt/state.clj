(ns jbittencourt.state
  (:require
   [io.github.humbleui.window :as window]))


(def *window
  "State of the main window. Gets set on app startup."
  (atom nil))

(def *app
  "Current state of what's drawn in the main app window.
  Gets set any time we want to draw something new."
  (atom nil))

(defn redraw!
  "Redraws the window with the current app state."
  []
  ;; we redraw only when window state has been set.
  ;; this lets us call the function on ns eval and will only
  ;; redraw if the window has already been created in either
  ;; user/-main or the app -main
  (some-> *window deref window/request-frame))
