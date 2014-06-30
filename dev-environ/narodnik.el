(defun lein-server () (interactive)
  (async-shell-command "lein ring server"))
(defun lein-run () (interactive)
  (async-shell-command "lein run"))

(global-set-key (kbd "C-2") 'lein-server)
(global-set-key (kbd "C-3") 'lein-run)

(defun use-hs-mode () (interactive)
  (hs-minor-mode t)
  (hs-hide-all))

(add-hook 'clojure-mode 'use-hs-mode)

(defun fold-sexp-forward () (interactive)
  (forward-sexp)
  (backward-sexp)
  (beginning-of-line)
  (hs-hide-block)
  (forward-sexp))

(defun fold-sexp-backward () (interactive)
  (backward-sexp)
  (hs-show-block))

(global-set-key (kbd "M-e") 'fold-sexp-forward)
(global-set-key (kbd "M-d") 'fold-sexp-backward)



