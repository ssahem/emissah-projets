-- ==============================================================================
-- V1__baseline.sql — schéma source des interventions terrain
--
-- Reproduit exactement le schéma public : 20 tables avec identités, 6 vues,
-- contraintes nommées et index. Les 8 fonctions et 11 triggers du schéma source
-- sont volontairement écartés (E-24) : les règles sont implémentées en service.
--
-- NE JAMAIS MODIFIER CETTE MIGRATION (baseline Flyway).
-- ==============================================================================

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adresse; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.adresse (
    id_adresse integer NOT NULL,
    voie character varying(255) NOT NULL,
    latitude numeric(9,6),
    longitude numeric(9,6),
    commune character varying NOT NULL,
    CONSTRAINT adresse_latitude_check CHECK (((latitude >= ('-90'::integer)::numeric) AND (latitude <= (90)::numeric))),
    CONSTRAINT adresse_longitude_check CHECK (((longitude >= ('-180'::integer)::numeric) AND (longitude <= (180)::numeric)))
);


--
-- Name: adresse_id_adresse_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.adresse ALTER COLUMN id_adresse ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.adresse_id_adresse_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: affectation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.affectation (
    id_affectation integer NOT NULL,
    est_responsable boolean DEFAULT false NOT NULL,
    id_intervention integer NOT NULL,
    id_technicien integer NOT NULL
);


--
-- Name: affectation_id_affectation_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.affectation ALTER COLUMN id_affectation ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.affectation_id_affectation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: agence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agence (
    id_agence integer NOT NULL,
    nom character varying(100) NOT NULL,
    adresse character varying(255) NOT NULL
);


--
-- Name: agence_id_agence_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.agence ALTER COLUMN id_agence ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.agence_id_agence_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: client; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.client (
    id_client integer NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    email character varying(255) NOT NULL,
    telephone character varying(20)
);


--
-- Name: client_id_client_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.client ALTER COLUMN id_client ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.client_id_client_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: commune; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commune (
    id_commune integer NOT NULL,
    nom character varying(100) NOT NULL,
    code_postal character varying(10) NOT NULL,
    id_departement integer NOT NULL,
    id_agence integer NOT NULL
);


--
-- Name: commune_id_commune_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.commune ALTER COLUMN id_commune ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.commune_id_commune_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: competence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.competence (
    id_competence integer NOT NULL,
    libelle character varying(100) NOT NULL
);


--
-- Name: competence_id_competence_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.competence ALTER COLUMN id_competence ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.competence_id_competence_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: departement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.departement (
    id_departement integer NOT NULL,
    nom character varying(100) NOT NULL,
    id_region integer NOT NULL
);


--
-- Name: departement_id_departement_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.departement ALTER COLUMN id_departement ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.departement_id_departement_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: depot; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.depot (
    id_depot integer NOT NULL,
    nom character varying(100) NOT NULL,
    adresse character varying(255) NOT NULL,
    id_agence integer NOT NULL
);


--
-- Name: depot_id_depot_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.depot ALTER COLUMN id_depot ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.depot_id_depot_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: equipe; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.equipe (
    id_equipe integer NOT NULL,
    nom character varying(100) NOT NULL,
    id_agence integer NOT NULL
);


--
-- Name: equipe_id_equipe_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.equipe ALTER COLUMN id_equipe ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.equipe_id_equipe_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: exigence_competence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exigence_competence (
    id_type integer NOT NULL,
    id_competence integer NOT NULL
);


--
-- Name: historique_statut; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.historique_statut (
    id_historique integer NOT NULL,
    date_heure timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_statut integer NOT NULL,
    id_affectation integer NOT NULL
);


--
-- Name: historique_statut_id_historique_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.historique_statut ALTER COLUMN id_historique ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.historique_statut_id_historique_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: intervention; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.intervention (
    id_intervention integer NOT NULL,
    date_demande timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    debut_prevu timestamp without time zone NOT NULL,
    fin_prevue timestamp without time zone NOT NULL,
    note_client smallint,
    commentaire_client text,
    id_client integer NOT NULL,
    id_adresse integer NOT NULL,
    id_intervention_origine integer,
    code_type_intervention character varying NOT NULL,
    CONSTRAINT ck_intervention_creneau CHECK ((fin_prevue > debut_prevu)),
    CONSTRAINT ck_intervention_pas_son_origine CHECK (((id_intervention_origine IS NULL) OR (id_intervention_origine <> id_intervention))),
    CONSTRAINT intervention_note_client_check CHECK (((note_client >= 1) AND (note_client <= 5)))
);


--
-- Name: intervention_id_intervention_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.intervention ALTER COLUMN id_intervention ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.intervention_id_intervention_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: obtention_competence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.obtention_competence (
    id_obtention integer NOT NULL,
    date_obtention date NOT NULL,
    date_expiration date,
    id_technicien integer NOT NULL,
    id_competence integer NOT NULL,
    CONSTRAINT ck_obtention_dates CHECK (((date_expiration IS NULL) OR (date_expiration > date_obtention)))
);


--
-- Name: obtention_competence_id_obtention_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.obtention_competence ALTER COLUMN id_obtention ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.obtention_competence_id_obtention_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: piece; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.piece (
    id_piece integer NOT NULL,
    code_piece character varying(50) CONSTRAINT piece_reference_not_null NOT NULL,
    libelle character varying(150) NOT NULL
);


--
-- Name: piece_id_piece_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.piece ALTER COLUMN id_piece ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.piece_id_piece_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: region; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.region (
    id_region integer NOT NULL,
    nom character varying(100) NOT NULL
);


--
-- Name: region_id_region_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.region ALTER COLUMN id_region ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.region_id_region_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: statut; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.statut (
    id_statut integer NOT NULL,
    libelle character varying(30) NOT NULL
);


--
-- Name: statut_id_statut_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.statut ALTER COLUMN id_statut ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.statut_id_statut_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: stock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock (
    id_stock integer NOT NULL,
    quantite integer NOT NULL,
    id_depot integer NOT NULL,
    id_piece integer NOT NULL,
    CONSTRAINT stock_quantite_check CHECK ((quantite >= 0))
);


--
-- Name: stock_id_stock_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.stock ALTER COLUMN id_stock ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.stock_id_stock_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: technicien; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.technicien (
    id_technicien integer NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    email character varying(255) NOT NULL,
    telephone character varying(20),
    id_equipe integer NOT NULL
);


--
-- Name: technicien_id_technicien_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.technicien ALTER COLUMN id_technicien ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.technicien_id_technicien_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: type_intervention; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.type_intervention (
    id_type integer NOT NULL,
    libelle character varying(100) NOT NULL,
    delai_heures integer NOT NULL,
    code character varying NOT NULL,
    CONSTRAINT type_intervention_delai_heures_check CHECK ((delai_heures > 0))
);


--
-- Name: type_intervention_id_type_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.type_intervention ALTER COLUMN id_type ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.type_intervention_id_type_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: utilisation_piece; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.utilisation_piece (
    id_utilisation integer NOT NULL,
    quantite integer NOT NULL,
    id_intervention integer NOT NULL,
    code_piece character varying NOT NULL,
    CONSTRAINT utilisation_piece_quantite_check CHECK ((quantite > 0))
);


--
-- Name: utilisation_piece_id_utilisation_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.utilisation_piece ALTER COLUMN id_utilisation ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.utilisation_piece_id_utilisation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: v_affectation_non_conforme; Type: VIEW; Schema: public; Owner: -
--
CREATE VIEW public.v_affectation_non_conforme AS
 SELECT a.id_affectation,
    a.id_intervention,
    a.id_technicien,
    e.id_competence AS competence_manquante
   FROM (((public.affectation a
     JOIN public.intervention i ON ((i.id_intervention = a.id_intervention)))
     JOIN public.type_intervention t ON (((t.code)::text = (i.code_type_intervention)::text)))
     JOIN public.exigence_competence e ON ((e.id_type = t.id_type)))
  WHERE (NOT (EXISTS ( SELECT 1
           FROM public.obtention_competence o
          WHERE ((o.id_technicien = a.id_technicien) AND (o.id_competence = e.id_competence) AND (o.date_obtention <= (i.debut_prevu)::date) AND ((o.date_expiration IS NULL) OR (o.date_expiration >= (i.fin_prevue)::date))))));


--
-- Name: v_agence_region; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_agence_region AS
 SELECT DISTINCT c.id_agence,
    d.id_region
   FROM (public.commune c
     JOIN public.departement d ON ((d.id_departement = c.id_departement)))
  WHERE (c.id_agence IS NOT NULL);


--
-- Name: v_intervention_delai; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_intervention_delai AS
 WITH fin AS (
         SELECT a.id_intervention,
            max(h.date_heure) AS date_fin_reelle
           FROM ((public.affectation a
             JOIN public.historique_statut h ON ((h.id_affectation = a.id_affectation)))
             JOIN public.statut s ON ((s.id_statut = h.id_statut)))
          WHERE ((s.libelle)::text ~~* 'termin%'::text)
          GROUP BY a.id_intervention
        )
 SELECT i.id_intervention,
    c.id_agence,
    i.date_demande,
    f.date_fin_reelle,
    (ceil((EXTRACT(epoch FROM (f.date_fin_reelle - i.date_demande)) / (3600)::numeric)))::integer AS delai_heures,
    ((f.date_fin_reelle - i.date_demande) <= ((t.delai_heures)::double precision * '01:00:00'::interval)) AS delai_respecte
   FROM ((((public.intervention i
     JOIN fin f ON ((f.id_intervention = i.id_intervention)))
     JOIN public.type_intervention t ON (((t.code)::text = (i.code_type_intervention)::text)))
     JOIN public.adresse ad ON ((ad.id_adresse = i.id_adresse)))
     LEFT JOIN public.commune c ON (((c.nom)::text = (ad.commune)::text)));


--
-- Name: v_delai_par_agence_mois; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_delai_par_agence_mois AS
 SELECT id_agence,
    date_trunc('month'::text, date_fin_reelle) AS mois,
    count(*) AS nb_interventions,
    count(*) FILTER (WHERE delai_respecte) AS nb_respectees,
    round(((100.0 * (count(*) FILTER (WHERE delai_respecte))::numeric) / (count(*))::numeric), 1) AS taux_respect_pct
   FROM public.v_intervention_delai d
  GROUP BY id_agence, (date_trunc('month'::text, date_fin_reelle));


--
-- Name: v_intervention_statut; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_intervention_statut AS
 SELECT DISTINCT ON (a.id_intervention) a.id_intervention,
    s.libelle AS statut,
    h.date_heure
   FROM ((public.affectation a
     JOIN public.historique_statut h ON ((h.id_affectation = a.id_affectation)))
     JOIN public.statut s ON ((s.id_statut = h.id_statut)))
  ORDER BY a.id_intervention, h.date_heure DESC, h.id_historique DESC;


--
-- Name: v_pieces_manquantes; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_pieces_manquantes AS
 SELECT u.id_intervention,
    u.code_piece,
    u.quantite AS quantite_necessaire,
    COALESCE(s.quantite, 0) AS quantite_en_stock
   FROM ((((((public.utilisation_piece u
     JOIN public.intervention i ON ((i.id_intervention = u.id_intervention)))
     JOIN public.adresse ad ON ((ad.id_adresse = i.id_adresse)))
     LEFT JOIN public.commune c ON (((c.nom)::text = (ad.commune)::text)))
     LEFT JOIN public.depot dp ON ((dp.id_agence = c.id_agence)))
     LEFT JOIN public.piece p ON (((p.code_piece)::text = (u.code_piece)::text)))
     LEFT JOIN public.stock s ON (((s.id_depot = dp.id_depot) AND (s.id_piece = p.id_piece))))
  WHERE (COALESCE(s.quantite, 0) < u.quantite);


--
-- Name: adresse adresse_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adresse
    ADD CONSTRAINT adresse_pkey PRIMARY KEY (id_adresse);


--
-- Name: affectation affectation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT affectation_pkey PRIMARY KEY (id_affectation);


--
-- Name: agence agence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agence
    ADD CONSTRAINT agence_pkey PRIMARY KEY (id_agence);


--
-- Name: client client_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_email_key UNIQUE (email);


--
-- Name: client client_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_pkey PRIMARY KEY (id_client);


--
-- Name: commune commune_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commune
    ADD CONSTRAINT commune_pkey PRIMARY KEY (id_commune);


--
-- Name: competence competence_libelle_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.competence
    ADD CONSTRAINT competence_libelle_key UNIQUE (libelle);


--
-- Name: competence competence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.competence
    ADD CONSTRAINT competence_pkey PRIMARY KEY (id_competence);


--
-- Name: departement departement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departement
    ADD CONSTRAINT departement_pkey PRIMARY KEY (id_departement);


--
-- Name: depot depot_id_agence_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.depot
    ADD CONSTRAINT depot_id_agence_key UNIQUE (id_agence);


--
-- Name: depot depot_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.depot
    ADD CONSTRAINT depot_pkey PRIMARY KEY (id_depot);


--
-- Name: equipe equipe_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipe
    ADD CONSTRAINT equipe_pkey PRIMARY KEY (id_equipe);


--
-- Name: exigence_competence exigence_competence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exigence_competence
    ADD CONSTRAINT exigence_competence_pkey PRIMARY KEY (id_type, id_competence);


--
-- Name: historique_statut historique_statut_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historique_statut
    ADD CONSTRAINT historique_statut_pkey PRIMARY KEY (id_historique);


--
-- Name: intervention intervention_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intervention
    ADD CONSTRAINT intervention_pkey PRIMARY KEY (id_intervention);


--
-- Name: obtention_competence obtention_competence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.obtention_competence
    ADD CONSTRAINT obtention_competence_pkey PRIMARY KEY (id_obtention);


--
-- Name: piece piece_code_piece_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.piece
    ADD CONSTRAINT piece_code_piece_key UNIQUE (code_piece);


--
-- Name: piece piece_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.piece
    ADD CONSTRAINT piece_pkey PRIMARY KEY (id_piece);


--
-- Name: region region_nom_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.region
    ADD CONSTRAINT region_nom_key UNIQUE (nom);


--
-- Name: region region_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.region
    ADD CONSTRAINT region_pkey PRIMARY KEY (id_region);


--
-- Name: statut statut_libelle_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.statut
    ADD CONSTRAINT statut_libelle_key UNIQUE (libelle);


--
-- Name: statut statut_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.statut
    ADD CONSTRAINT statut_pkey PRIMARY KEY (id_statut);


--
-- Name: stock stock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_pkey PRIMARY KEY (id_stock);


--
-- Name: technicien technicien_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technicien
    ADD CONSTRAINT technicien_email_key UNIQUE (email);


--
-- Name: technicien technicien_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technicien
    ADD CONSTRAINT technicien_pkey PRIMARY KEY (id_technicien);


--
-- Name: type_intervention type_intervention_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_intervention
    ADD CONSTRAINT type_intervention_code_key UNIQUE (code);


--
-- Name: type_intervention type_intervention_libelle_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_intervention
    ADD CONSTRAINT type_intervention_libelle_key UNIQUE (libelle);


--
-- Name: type_intervention type_intervention_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_intervention
    ADD CONSTRAINT type_intervention_pkey PRIMARY KEY (id_type);


--
-- Name: affectation uq_affectation_intervention_technicien; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT uq_affectation_intervention_technicien UNIQUE (id_intervention, id_technicien);


--
-- Name: commune uq_commune_nom; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commune
    ADD CONSTRAINT uq_commune_nom UNIQUE (nom);


--
-- Name: obtention_competence uq_obtention; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.obtention_competence
    ADD CONSTRAINT uq_obtention UNIQUE (id_technicien, id_competence, date_obtention);


--
-- Name: stock uq_stock_depot_piece; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT uq_stock_depot_piece UNIQUE (id_depot, id_piece);


--
-- Name: utilisation_piece uq_utilisation_intervention_piece; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisation_piece
    ADD CONSTRAINT uq_utilisation_intervention_piece UNIQUE (id_intervention, code_piece);


--
-- Name: utilisation_piece utilisation_piece_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisation_piece
    ADD CONSTRAINT utilisation_piece_pkey PRIMARY KEY (id_utilisation);


--
-- Name: idx_historique_affectation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_historique_affectation ON public.historique_statut USING btree (id_affectation, date_heure);


--
-- Name: idx_intervention_debut; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_intervention_debut ON public.intervention USING btree (debut_prevu);


--
-- Name: ux_un_responsable_par_intervention; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_un_responsable_par_intervention ON public.affectation USING btree (id_intervention) WHERE est_responsable;
--
-- Name: affectation affectation_id_intervention_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT affectation_id_intervention_fkey FOREIGN KEY (id_intervention) REFERENCES public.intervention(id_intervention);


--
-- Name: affectation affectation_id_technicien_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affectation
    ADD CONSTRAINT affectation_id_technicien_fkey FOREIGN KEY (id_technicien) REFERENCES public.technicien(id_technicien);


--
-- Name: commune commune_id_agence_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commune
    ADD CONSTRAINT commune_id_agence_fkey FOREIGN KEY (id_agence) REFERENCES public.agence(id_agence);


--
-- Name: commune commune_id_departement_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commune
    ADD CONSTRAINT commune_id_departement_fkey FOREIGN KEY (id_departement) REFERENCES public.departement(id_departement);


--
-- Name: departement departement_id_region_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departement
    ADD CONSTRAINT departement_id_region_fkey FOREIGN KEY (id_region) REFERENCES public.region(id_region);


--
-- Name: depot depot_id_agence_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.depot
    ADD CONSTRAINT depot_id_agence_fkey FOREIGN KEY (id_agence) REFERENCES public.agence(id_agence);


--
-- Name: equipe equipe_id_agence_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipe
    ADD CONSTRAINT equipe_id_agence_fkey FOREIGN KEY (id_agence) REFERENCES public.agence(id_agence);


--
-- Name: exigence_competence exigence_competence_id_competence_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exigence_competence
    ADD CONSTRAINT exigence_competence_id_competence_fkey FOREIGN KEY (id_competence) REFERENCES public.competence(id_competence);


--
-- Name: exigence_competence exigence_competence_id_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exigence_competence
    ADD CONSTRAINT exigence_competence_id_type_fkey FOREIGN KEY (id_type) REFERENCES public.type_intervention(id_type);


--
-- Name: intervention fk_intervention_origine; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intervention
    ADD CONSTRAINT fk_intervention_origine FOREIGN KEY (id_intervention_origine) REFERENCES public.intervention(id_intervention);


--
-- Name: historique_statut historique_statut_id_affectation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historique_statut
    ADD CONSTRAINT historique_statut_id_affectation_fkey FOREIGN KEY (id_affectation) REFERENCES public.affectation(id_affectation);


--
-- Name: historique_statut historique_statut_id_statut_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historique_statut
    ADD CONSTRAINT historique_statut_id_statut_fkey FOREIGN KEY (id_statut) REFERENCES public.statut(id_statut);


--
-- Name: intervention intervention_id_adresse_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intervention
    ADD CONSTRAINT intervention_id_adresse_fkey FOREIGN KEY (id_adresse) REFERENCES public.adresse(id_adresse);


--
-- Name: intervention intervention_id_client_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intervention
    ADD CONSTRAINT intervention_id_client_fkey FOREIGN KEY (id_client) REFERENCES public.client(id_client);


--
-- Name: obtention_competence obtention_competence_id_competence_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.obtention_competence
    ADD CONSTRAINT obtention_competence_id_competence_fkey FOREIGN KEY (id_competence) REFERENCES public.competence(id_competence);


--
-- Name: obtention_competence obtention_competence_id_technicien_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.obtention_competence
    ADD CONSTRAINT obtention_competence_id_technicien_fkey FOREIGN KEY (id_technicien) REFERENCES public.technicien(id_technicien);


--
-- Name: stock stock_id_depot_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_id_depot_fkey FOREIGN KEY (id_depot) REFERENCES public.depot(id_depot);


--
-- Name: stock stock_id_piece_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_id_piece_fkey FOREIGN KEY (id_piece) REFERENCES public.piece(id_piece);


--
-- Name: technicien technicien_id_equipe_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technicien
    ADD CONSTRAINT technicien_id_equipe_fkey FOREIGN KEY (id_equipe) REFERENCES public.equipe(id_equipe);


--
-- Name: utilisation_piece utilisation_piece_id_intervention_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisation_piece
    ADD CONSTRAINT utilisation_piece_id_intervention_fkey FOREIGN KEY (id_intervention) REFERENCES public.intervention(id_intervention);


--
-- PostgreSQL database dump complete
--

