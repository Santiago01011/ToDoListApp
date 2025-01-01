--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: tasks; Type: TABLE; Schema: public; Owner: postgres
--

DROP TABLE IF EXISTS public.tasks
CREATE TABLE public.tasks (
    id integer NOT NULL,
    task_title character varying(50) NOT NULL,
    description TEXT,
    date_added TIMESTAMP without time zone NOT NULL,
    deleted_at TIMESTAMP without time zone,
    is_done BOOLEAN NOT NULL,
    user_id INTEGER NOT NULL,
);


ALTER TABLE public.tasks OWNER TO postgres;

--
-- Name: tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tasks_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tasks_id_seq OWNER TO postgres;

--
-- Name: tasks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tasks_id_seq OWNED BY public.tasks.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--
DROP TABLE IF EXISTS public.users
CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(50) NOT NULL,
    email TEXT UNIQUE
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: tasks id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tasks ALTER COLUMN id SET DEFAULT nextval('public.tasks_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: tasks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tasks (id, task_title, description, date_added, is_done, user_id) FROM stdin;
30	test history		2024-12-20 17:32:45.371794	t	1
55	Create a PSQL database in the cloud	Scale the project to conect to a cloud database to work without the local instance	2024-12-29 20:29:35.797303	t	1
53	Night cursor	Change the cursor for night mode, to be more visible	2024-12-29 15:51:22.032253	t	1
48	Day-Night	Solve the conflicts with the day/night mode	2024-12-27 21:57:30.199885	t	1
38	Lorem Ipsum	Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. BOCA BOCA BOCA BOCA BOCA BOCA	2024-12-27 14:01:12.293161	t	1
47	TaskDetailsArea	Improve the task details area, put padding	2024-12-27 20:35:56.25227	t	1
44	Login Frame	Made the login extends Frame	2024-12-27 20:33:01.788382	t	1
43	Login-Register	Made the Login/Register interface, and connect to the database to validate.	2024-12-27 20:32:11.349088	t	1
52	Release test	Make the JAR file work, with all the functions and icons	2024-12-29 15:50:33.479793	t	1
56	Folders	Solve the folders structure problem	2024-12-30 01:49:55.744146	t	1
46	Edit Tasks	Make the functionality to edit tasks	2024-12-27 20:34:28.740121	f	1
58	DB security	Implement security for the acces to the database	2024-12-30 16:05:28.99162	f	1
57	DB connections	Decrement the number of connections needed	2024-12-30 01:50:46.357972	f	1
51	Register	Registered successfully	2024-12-29 02:13:21.846642	t	3
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password) FROM stdin;
1	Santiago	boca
2	Ñoqui	Polenta
3	TestRegister	register
4	TestIcons	icons
5	Riquelme	boca
\.


--
-- Name: tasks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.tasks_id_seq', 58, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 5, true);


--
-- Name: tasks tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: tasks tasks_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

