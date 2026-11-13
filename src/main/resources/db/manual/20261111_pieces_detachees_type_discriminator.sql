-- Aligne la colonne discriminante de l'héritage JPA SINGLE_TABLE
-- sur @DiscriminatorColumn(name = "type") de PieceDetache.
DO $$
DECLARE
    has_type_piece boolean;
    has_type boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'pieces_detachees'
          AND column_name = 'type_piece'
    ) INTO has_type_piece;

    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'pieces_detachees'
          AND column_name = 'type'
    ) INTO has_type;

    IF has_type_piece AND NOT has_type THEN
        ALTER TABLE pieces_detachees RENAME COLUMN type_piece TO "type";
    ELSIF has_type_piece AND has_type THEN
        RAISE EXCEPTION 'Migration interrompue : pieces_detachees contient déjà les colonnes type_piece et type';
    ELSIF NOT has_type THEN
        RAISE EXCEPTION 'Migration impossible : aucune colonne type_piece ou type dans pieces_detachees';
    END IF;
END
$$;
