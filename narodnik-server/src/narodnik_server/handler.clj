(ns narodnik-server.handler
  (:use [aleph.udp]
        [gloss core io]
        [lamina core api]))

(defn message-thread []
  (let [a @(udp-object-socket {:port 2000})
        b @(udp-object-socket)
        text-msg "THIS IS THE PACKET"]
    (try
      (enqueue b {:message text-msg :host "localhost" :port 2000})
      (println (:message (wait-for-message a 2000)))
      (finally
        (close b)
        (close a)
      ))))

(defn -main [& args]
  (.start (Thread. message-thread))
  (println "NARODNIK SERVER!"))
