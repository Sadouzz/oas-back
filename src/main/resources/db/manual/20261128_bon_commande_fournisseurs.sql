BEGIN;

LOCK TABLE public.bons_de_commande, public.fournisseur, public.fournisseurs
    IN SHARE ROW EXCLUSIVE MODE;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.bons_de_commande WHERE fournisseur_id IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Vérifier la correspondance métier des fournisseurs des commandes existantes avant de déplacer la clé étrangère';
    END IF;
END
$$;

ALTER TABLE public.bons_de_commande
    DROP CONSTRAINT fkpm226jfhx75uwuymea212hwn,
    ADD CONSTRAINT fk_bons_de_commande_fournisseurs
        FOREIGN KEY (fournisseur_id) REFERENCES public.fournisseurs(id);

COMMIT;
