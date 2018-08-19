(ns narodnik-core.data
  (:use [lamina core api]
        [narodnik-core library])
  (:require 
   [clojure.java.jdbc :as sql]))

(def narodnik-schema 
  [

                      [:machine
                       [:name :text]
                       [:status :text] ; invite recieved, free, busy; as key in clj
                       [:privatekey :text]
                       [:publickey :text]
                       [:hostid :bigint]]

                      [:host
                       [:id :bigint]
                       [:address :text]
                       [:port :int]]

                      [:machinegroup 
                       [:id :bigint]
                       [:name :text]]

                      [:groupmember 
                       [:groupid :bigint]
                       [:machineid :bigint]]

                      [:chunk
                       [:taskid :bigint]
                       [:content :text]
                       [:index :bigint]]

                      [:task
                       [:id :bigint]
                       [:content :text] ; clojure/json serialized s-exp/obj/message
                       [:starttime :text] ; time of execution on host machine
                       ]
                      
                      [:job
                       [:actortype :text] ; machine/group
                       [:actorid :text] 
                       [:taskid :bigint]
                       [:status :text]    ; undone,assigned,in progress,done
                       [:starttime :text] ; time of delivery to host machine
                       [:endtime :text]] ; time completed

                      [:dictionary ; for general purpose storage and status flags
                       [:key :text]
                       [:value :text]
                       [:counter :bigint]
                       [:collectionid :text]
                       [:machineid :bigint]]
])

(java.security.Security/addProvider
 (org.bouncycastle.jce.provider.BouncyCastleProvider.))

(def generate-secure-key []
     "" 
     )

(defn generate-keys []
  (let [generator (doto (java.security.KeyPairGenerator/getInstance "RSA" "BC")
                    (.initialize 1024))]
		    (.generateKeyPair generator)))

(defn read-keys [f]
  (-> f
      java.io.FileReader.
      org.bouncycastle.openssl.PEMReader.
      .readObject))

(defn write-keys [f keys]
  (let [writer (org.bouncycastle.openssl.PEMWriter. (java.io.FileWriter. f))]
    (.writeObject writer keys)
    (.flush writer)))

(defn encrypt [bytes public-key]
  (let [cipher (doto (javax.crypto.Cipher/getInstance "RSA/ECB/PKCS1Padding" "BC")
                 (.init javax.crypto.Cipher/ENCRYPT_MODE public-key))]
    (.doFinal cipher bytes)))


(defn decrypt [bytes private-key]
  (let [cipher (doto (javax.crypto.Cipher/getInstance "RSA/ECB/PKCS1Padding" "BC")
                 (.init javax.crypto.Cipher/DECRYPT_MODE private-key))]
		 (.doFinal cipher bytes)))

(defn read-to-byte [f]
  (let [file (java.io.RandomAccessFile. f "r")
       content (make-array Byte/TYPE (.length file))]
       (.read file content)
       content))

(def keys (generate-keys))

(def encrypted (encrypt (.getBytes "Too Many Secrets") (.getPublic keys)))
(String. (decrypt encrypted (.getPrivate keys)))


(def signuture (sign (.getBytes "Too Many Secrets") (.getPrivate keys)))
(verify signuture (.getBytes "Too Many Secrets") (.getPublic keys))


(def keys (read-keys (java.io.File. "/Users/nakkaya/Dropbox/kiler/rsa.pem")))

(def key-string [file]
  (String. (decrypt
	    (read-to-byte
	     (java.io.File. file))
	    (.getPrivate keys)))
  )


(let [db-host "localhost"
        db-port 5432
        db-name "narodnik"]
  (def database {
           :subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user "postgres"
           :password (generate-secure-key)}))

(def create-table sql/create-table-ddl)

(def exec-sql! sql/db-do-commands)

(def query-db sql/query) 

(def insert-db! sql/insert!)

(defn create-tables [] 
  (println "Creating tables...")
  (dorun (map (fn [table-design] 
                (try (exec-sql! database (apply create-table table-design))
                     (catch Exception ex (println "Could not create table" 
                                                  (str table-design)
                                                  (str ex)))))
              narodnik-schema)))

(defn query-db [db query]
  (println "Excecuting query : " query)
  (sql/query db query))

(defn db-select [table key value]
  (let [query 
               (str "select * from " (name table)
                   " where " (name key) "=" (str value))]
    (query-db database query)))

(defn db-select-all [table]
  (sql/query database 
              (str "select * from " (name table))))

(defn db-insert! [table object]
  (insert-db! database table object))

(defn db-update! [table condition value update-map ]
  (sql/update! database
    table update-map [(str (name condition) "=?") value] ))
          
(defn db-generate-id [table] 
  (int (rand 100000000)))

(defn drop-all-tables [] 
  (println "Dropping all tables...")
  (exec-sql! database "drop schema public cascade")
  (exec-sql! database "create schema public"))

(defn init-db []
  (println "Setting up database...")
  (create-tables))

(defn most-important-undone-job [] "temp for assignment thread"
  (first (db-select :job :status "'undone'")))

(defn get-task-of-job [job]
  (first (db-select :task :id (:taskid job))))

(defn get-job-by-taskid [taskid]
  (first (db-select :job :taskid taskid)))

(defn get-machine [name]
  (first (db-select :machine :name (str "'" name "'"))))

(defn exists-machine? [name] 
  (not (empty? (db-select :machine :name (str "'" name "'")))))

(defn datetime-now [] (str (new java.util.Date)))

(defn get-host-of-machine [machine]
  (println "Looking up host of machine : " (str machine))
  (first (db-select :host :id (:hostid machine))))

(defn get-actor-of-job [job]
  (println "Looking up actor for job.")
  (get-machine (:actorid job)))
