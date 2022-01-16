--resetting data schema!
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

--creating tables

create table hospital(hosp_name varchar(30),
                      hosp_city varchar(30),
                      hosp_state varchar(30),
                      hosp_cost numeric(30, 2),
                      primary key (hosp_name, hosp_city, hosp_state));

create table patient (patient_id int,
                     patient_name varchar(30),
                     doctor_id int not null, --fk for table doctor
                     organ_name varchar(30) not null, --fk for table organ
                     donor_id int not null, --fk for table organ_donor
                     blood_type varchar(3) not null, --fk for table organ_donor
                     hosp_name varchar(30) not null, --fk for table hospital
                     hosp_city varchar(30) not null, --fk for table hospital
                     hosp_state varchar(30) not null, --fk for table hospital
                     age varchar(3),
                     primary key (patient_id));
                    
create table organ (organ_name varchar(30),
                     primary key (organ_name));
                    
create table doctor (doctor_id int UNIQUE,
                     doctor_name varchar(30),
                     organ_name varchar(30),
                     hosp_name varchar(30) not null, --fk for table hospital
                     hosp_city varchar(30) not null, --fk for table hospital
                     hosp_state varchar(30) not null, --fk for table hospital
                     fee numeric(10,2),
                     primary key (doctor_id, hosp_name, hosp_city, hosp_state));

create table organ_donor(donor_id int,
                      donor_name varchar(30),
                      blood_type varchar(3),
                      doctor_id int not null, --fk for table doctor
                      age int,
                      chronical_diseases varchar(30),
                      drug_usage varchar(30),
                      tattoo_date date,
                      med_history varchar(500),
                      last_donation_time date,
                      city varchar(30),
                      state varchar(30),
                      primary key (donor_id, blood_type));

create table organ_donor_organs(donor_id int,		--multivalued attribute for organs belonging to donor
                               blood_type varchar(3),
                               avail_date date,
                               organ_name varchar(30) not null); --fk for table organ
                     
create table organ_donor_contact_info(donor_id int,	--multivalued attribute for contact info of donor
                                     blood_type varchar(3),
                                     contact_info varchar(30));

--adding forgein keys to respective tables

alter table patient
add foreign key (doctor_id, hosp_name, hosp_city, hosp_state) references doctor(doctor_id, hosp_name, hosp_city, hosp_state),
add foreign key (organ_name) references organ(organ_name);

alter table doctor
add foreign key (hosp_name, hosp_city, hosp_state) references hospital(hosp_name, hosp_city, hosp_state),
add foreign key (organ_name) references organ(organ_name);

alter table organ_donor 
add foreign key (doctor_id) references doctor(doctor_id);

alter table organ_donor_organs
add foreign key (donor_id, blood_type) references organ_donor(donor_id, blood_type),
add foreign key (organ_name) references organ(organ_name);

alter table organ_donor_contact_info 
add foreign key (donor_id, blood_type) references organ_donor(donor_id, blood_type);

--creating roles for authorization

create user doctor with password '1234';
GRANT USAGE ON SCHEMA public TO doctor;
GRANT CREATE ON SCHEMA public TO doctor;
grant select on table hospital to doctor;
grant select on table organ to doctor;
grant select on table organ_donor to doctor;
grant select on table organ_donor_contact_info to doctor;
grant select on table patient to doctor;
grant select on table public.organ_donor_organs to doctor;
grant insert on table public.organ_donor_organs to doctor; --in case of a death, the doctor should be able to add the available donor's organs
GRANT INSERT ON TABLE public.organ TO doctor;
GRANT SELECT ON TABLE public.doctor TO doctor;
GRANT INSERT ON TABLE public.organ_donor TO doctor;
GRANT INSERT ON TABLE public.organ_donor_contact_info TO doctor;
GRANT INSERT ON TABLE public.patient TO doctor;


create user patient with password '4321';
GRANT USAGE ON SCHEMA public TO patient;
grant select on table public.organ to patient;
grant select on table public.organ_donor to patient;
grant select on table public.organ_donor_organs to patient;
grant select on table public.hospital to patient;

create user organ_donor with password '1212';
GRANT USAGE ON SCHEMA public TO organ_donor;
grant select on table public.organ to organ_donor;
grant select on table public.organ_donor to organ_donor;
grant select on table public.organ_donor_organs to organ_donor;
grant select on table public.hospital to patient;


create user server_admin with password '0000';
GRANT USAGE ON SCHEMA public TO server_admin;
GRANT create ON SCHEMA public TO server_admin;
ALTER ROLE server_admin SUPERUSER CREATEDB CREATEROLE INHERIT LOGIN;
GRANT ALL ON all TABLES IN SCHEMA public TO server_admin;

--creating indices 

--for quicker implementation of organ donor list
create index doctor_organ_name_idx on public.doctor (organ_name);
create index organ_donor_city_idx on public.organ_donor (city, state);
create index organ_donor_organs_organ_name_idx on public.organ_donor_organs (organ_name);

--for quicker implementation of blood donor list
--create index organ_donor_city_idx on public.organ_donor (city,state,blood_type,age);

--for quicker implementation of donor match list
create index donor_match_list_idx on public.organ_donor (state, blood_type);

--for quicker implementation of income report
create index hospital_hosp_cost_idx on public.hospital (hosp_cost);

--for quicker implementation of operations report
create index doctor_hosp_state_idx on public.doctor (hosp_state, hosp_city, doctor_name);


