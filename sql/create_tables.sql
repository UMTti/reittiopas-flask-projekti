CREATE TABLE Stop (
    id varchar(200) PRIMARY KEY,
    name varchar(100),
    beaconid varchar(100),
    location varchar(100)
);

CREATe TABLE User (
    sessionid varchar(100) PRIMARY KEY,
    location varchar(100)
);

CREATE TABLE Buss (
    beaconid varchar(100) PRIMARY KEY,
    route varchar(100)
);
