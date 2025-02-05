-- scripts/init.sql

-- Vérifier si la base de données existe et la créer si elle n'existe pas
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'applica')
BEGIN
    CREATE DATABASE applica;
END

-- Utiliser la base de données
USE applica;

-- Créer un login pour l'utilisateur applica_api
IF NOT EXISTS (SELECT name FROM sys.sql_logins WHERE name = 'applica_api')
BEGIN
    CREATE LOGIN applica_api WITH PASSWORD = 'YourStrong!Passw0rd';
END

-- Créer un utilisateur pour la base de données applica
IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'applica_api')
BEGIN
    CREATE USER applica_api FOR LOGIN applica_api;
    ALTER ROLE db_owner ADD MEMBER applica_api;
END