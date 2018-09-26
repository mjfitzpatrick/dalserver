update siav2model 
       set title="ALMA test data",
       creator = "NRAO",
       collection = "ivo://nrao/alma",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "phot.flux",
       fluxaxisunit = "JY/BEAM",
       polaxisucd = "phys.polarization.stokes"
       where archiveid like "alma%";

update siav2model 
       set title="JCMT test data",
       creator = "JCMT",
       collection = "ivo://cadc.nrc.ca/archive/jcmt",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "phot.antennaTemp",
       fluxaxisunit = "K",
       polaxisucd = ""
       where archiveid like "jcmt%";

update siav2model 
       set title="JVLA test data",
       creator = "NRAO",
       collection = "ivo://nrao/jvla",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "phot.flux",
       fluxaxisunit = "JY/BEAM",
       polaxisucd = ""
       where archiveid like "jvla%";

update siav2model 
       set title="JWST test data",
       creator = "STScI",
       collection = "ivo://stsci.edu/jwst",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "phot.count",
       fluxaxisunit = "DN",
       polaxisucd = ""
       where archiveid like "jw%";

update siav2model 
       set title="KECK test data from OSIRIS",
       creator = "KECK",
       collection = "ivo://msc.koa/osiris",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "",
       fluxaxisunit = "",
       polaxisucd = ""
       where archiveid like "keck%";

update siav2model 
       set title="CALIFA test data",
       creator = "NED",
       collection = "ivo://ned.ipac/califa",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "",
       fluxaxisunit = "",
       polaxisucd = ""
       where archiveid like "ned/califa%";

update siav2model 
       set title="Heracles test data",
       creator = "NED",
       collection = "ivo://ned.ipac/heracles",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "",
       fluxaxisunit = "K KM/S",
       polaxisucd = ""
       where archiveid like "ned/heracles%";

update siav2model 
       set title="NED miscellaneous test data",
       creator = "NED",
       collection = "ivo://ned.ipac/misc",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "",
       fluxaxisunit = "",
       polaxisucd = ""
       where archiveid like "ned/misc%";

update siav2model 
       set title="Sting test data",
       creator = "NED",
       collection = "ivo://ned.ipac/sting",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "phot.flux",
       fluxaxisunit = "JY/BEAM",
       polaxisucd = "phys.polarization.stokes"
       where archiveid like "ned/sting%";

update siav2model 
       set title="VLA test data",
       creator = "NRAO",
       collection = "ivo://nrao/vla",
       creationtype = "archival",
       publisherdid = concat("ivo://nrao/siav2test#", archiveid),
       fluxaxisucd = "phot.flux",
       fluxaxisunit = "JY/BEAM",
       polaxisucd = "phys.polarization.stokes"
       where archiveid like "vla%";

delete from siav2model where archiveid='temp.fits';

-- ===============================================

update siav2model
   set spectralstart=0.000003700, spectralstop=0.000007500,
       spectrallocation=0.000005600, spectralresolution=0.000000006
 where archiveid like "ned/califa%";

update siav2model
   set spatialresolution1=0.00372132751244, spatialresolution2=0.00372132751244
 where archiveid like "ned/heracles/%";
update siav2model
   set fluxaxisucd='phot.antennaTemp;phot.flux.density.sb'
 where archiveid like "ned/heracles/%hans.fits.gz";
update siav2model
   set fluxaxisucd='phot.antennaTemp;phot.flux.density.sb;spect.dopplerVeloc.radio'
 where archiveid like "ned/heracles/%mom0.fits.gz";
update siav2model
   set fluxaxisucd='spect.dopplerVeloc.radio'
 where archiveid like "ned/heracles/%mom1.fits.gz";

update siav2model
   set fluxaxisunit='JY/BEAM' where 
       fluxaxisunit='' and archiveid like 'ned/misc/%';

update siav2model
   set fluxaxisucd='phot.flux.density'
 where fluxaxisucd='' and fluxaxisunit='JY/BEAM';

update siav2model
   set polaxisucd='phys.polarization.stokes', polaxisenum='I'
 where wcsaxes4='STOKES';

-- ===============================================

update siav2model 
   set spectralstart=NULL,
       spectrallocation=NULL,
       spectralstop=NULL
 where spectrallocation=0 and spectralstop=0;
update siav2model
   set spectralresolution=NULL
 where spectralresolution=0;
update siav2model
   set spectralrespower=NULL
 where spectralrespower=0;
update siav2model
   set timestart=NULL where timestart=0;
update siav2model
   set timestop=NULL where timestop=0;

update siav2model
   set obsid=NULL where obsid='';
update siav2model
   set title=NULL where title='';
update siav2model
   set creator=NULL where creator='';
update siav2model
   set collection=NULL where collection='';
update siav2model
   set publisherdid=NULL where publisherdid='';
update siav2model
   set creationtype=NULL where creationtype='';
update siav2model
   set polaxisucd=NULL where polaxisucd='';
update siav2model
   set polaxisenum=NULL where polaxisenum='';
update siav2model
   set spatialresolution1=NULL where spatialresolution1=0;
update siav2model
   set spatialresolution2=NULL where spatialresolution2=0;
update siav2model
   set fluxaxisunit=NULL where fluxaxisunit='';

update siav2model
   set title=concat(title,': ',substring_index(archiveid,'/',-1));

update siav2model
   set naxes=(naxes-1) where naxis4 = 1;
update siav2model
   set naxes=(naxes-1) where naxis3 = 1;

update siav2model
   set pixelresolution1=abs(pixelresolution1)
 where pixelresolution1 < 0;
