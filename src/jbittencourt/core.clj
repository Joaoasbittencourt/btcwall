(ns jbittencourt.core
  (:require
   [io.github.humbleui.ui :as ui]
   [jbittencourt.state :as state]))


(defonce *clicks (atom 0))

(def app
  "Main app definition."
  (ui/default-theme
   {}
   (ui/focus-controller
    (ui/padding
     10
     (ui/column
      (ui/label "Wallet")
      (ui/gap 0 10)
      (ui/dynamic _ [clicks @*clicks]
                  (ui/label (str "Count:  " clicks)))
      (ui/gap 0 10)
      (ui/button #(swap! *clicks inc)
                 (ui/label "Increment"))

      (ui/gap 0 10)
      (ui/button #(swap! *clicks dec)
                 (ui/label "Decrement")))))))

(def window-config
  {:title    "Wallet"
   :bg-color 0xFFFFFFFF})

(defn -main [& _]
  (ui/start-app!
   (reset! state/*window (ui/window window-config state/*app)))
  (state/redraw!))

(reset! state/*app app)
(state/redraw!)

