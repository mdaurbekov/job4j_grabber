create table posts (id serial primary key,
                  name varchar(200),
                  text text,
                  link varchar(255) unique,
                  created timestamp
                  );
