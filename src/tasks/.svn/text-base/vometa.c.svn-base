#include <stdlib.h>
#include <getopt.h>
#include <string.h>
#include <ctype.h>
#include <float.h>
#include <math.h>
#include <sys/stat.h>
#include "fitsio.h"
#include "ast.h"

#define MAXAXES		8
#define	KEYLEN		FLEN_KEYWORD
#define	COMLEN		FLEN_COMMENT
#define RAD2DEG		57.2957795
#define	EPSILONR	(1.19e-7)
#define	EPSILOND	(2.22e-16)

/* Axis map. Maps logical axes in the axis map to physical
 * image axes.
 */
#define	AX_SP1		0	/* First spatial axis */
#define	AX_SP2		1	/* Second spatial axis */
#define	AX_EM		2	/* Spectral (EM) axis */
#define	AX_TIME		3	/* Time axis */
#define	AX_POL		4	/* Polarization axis */

#define abs(x)		((x)<0 ? -(x):(x))
#define min(x,y)	((x)<(y)?x:y)
#define max(x,y)	((x)>(y)?x:y)
#define nint(x)		((int)((x)+0.5))

/* Global params shared by all functions. */
static int debug = 0;
const char *program_name=NULL;

static AstFrameSet *read_header();
static void compute_metadata();
static int findAxis();
static double image2wave();
static void error();

/*
 * VOMETA - Compute standard SIAV2 metadata for a FITS image.
 *
 * This uses the generic FITS image and WCS metadata to compute standard VO
 * (SIAV2) metadata values.  No collection-specific metadata is computed.
 * If insufficient information is available to compute a given metadata value
 * then it is omitted.
 */
int
main(int argc, char *argv[]) {
    program_name = argv[0];
    char *imagefile = NULL;
    char *fname = NULL;
    int ch;

    /* Command line arguments. */
    static char keyopts[] = "df:";
    static struct option longopts[] = {
	{ "debug",	no_argument,		NULL,	'd' },
	{ "file",	required_argument,	NULL,	'f' },
	{ NULL,		0,			NULL,	0 },
    };

    /* Process command line options. */
    while ((ch = getopt_long(argc, argv, keyopts, longopts, NULL)) != -1) {
	switch (ch) {
	case 'd':
	    debug++;
	    break;
	case 'f':
	    fname = optarg;
	    break;
	default:
	    error(1, "unknown option", optarg);
	}
    }

    argc -= optind;
    argv += optind;

    /* Get the pathname of the image to be processed. */
    imagefile = argv[0];
    if (debug)
	printf("prog=%s, imagefile=%s\n", program_name, imagefile);

    /* Compute and output the image metadata. */
    if (imagefile == NULL)
	error(2, "no imagefile specified", NULL);
    else {
	FILE *fp = stdout;
	if (fname) {
	    if ((fp = fopen(fname, "w")) == NULL)
		error(3, "cannot open output file", fname);
	}
	compute_metadata (fp, imagefile);
	if (fp != stdout)
	    fclose (fp);
    }

    exit (0);
}


/*
 * Compute the SIAV2 image table metadata for an image, and write
 * it to the given output stream.
 */
static void
compute_metadata(FILE *fp, char *imagefile) {
    long naxis[MAXAXES], datalen;
    int real_naxes=0, npts=2, step=2;
    int axmap[MAXAXES], imdim, naxes, bitpix, i;
    double ra_cen, dec_cen;
    double ra1, dec1, ra2, dec2, ra3, dec3, ra4, dec4;
    char ctype[MAXAXES][KEYLEN]; char cunit[MAXAXES][KEYLEN];
    double in[MAXAXES*2], out[MAXAXES*2];
    char comment[MAXAXES][COMLEN], bunit[KEYLEN];
    int status1=0, status2=0;
    AstFrameSet *wcs;

    astBegin;

    /*
     * Collect standard information from the FITS header.
     * ---------------------------------------------------
     */

    /* Read the image geometry and WCS. */
    if ((wcs = read_header(imagefile, &naxes, naxis,
	    &bitpix, axmap, ctype, cunit, bunit, comment, MAXAXES)) == NULL) {
	error(5, "cannot read metadata for image", imagefile);
    }

    /* Identify the image axes. */
    int ra_axis = findAxis(AX_SP1, axmap, naxes);
    int dec_axis = findAxis(AX_SP2, axmap, naxes);
    int em_axis = findAxis(AX_EM, axmap, naxes);

    int ra_len = naxis[ra_axis];
    int dec_len = naxis[dec_axis];

    char *em_ctype = ctype[em_axis];
    if (em_ctype[0] == '\0')
	em_ctype = NULL;
    char *em_unit = cunit[em_axis];
    if (em_unit[0] == '\0')
	em_unit = NULL;

    /* Compute the world coordinates of the full image by taking the forward
     * transform of the full pixel array.  Due to the way array dimensioning
     * is used AST wants the coordinates of the two points to be interleaved, e.g.,
     * with a stepsize of 2, pt1-val1, pt2-val1, pt1-val2, pt2-val2, etc.
     */
    for (i=0, imdim=naxes;  i < imdim;  i++) {
	in[(i*step)+0] = 1.0;
	in[(i*step)+1] = naxis[i];
    }

    /* Compute RA,DEC in degrees of image corner 1,1. */
    in[(ra_axis*step)+0] = 1.0;
    in[(dec_axis*step)+0] = 1.0;
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra1 = out[(ra_axis*step)+0] * RAD2DEG;
    dec1 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Compute RA,DEC in degrees of image corner 2,2. */
    in[(ra_axis*step)+0] = ra_len;
    in[(dec_axis*step)+0] = 1.0;
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra2 = out[(ra_axis*step)+0] * RAD2DEG;
    dec2 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Compute RA,DEC in degrees of image corner 3,3. */
    in[(ra_axis*step)+0] = ra_len;
    in[(dec_axis*step)+0] = dec_len;
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra3 = out[(ra_axis*step)+0] * RAD2DEG;
    dec3 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Compute RA,DEC in degrees of image corner 4,4. */
    in[(ra_axis*step)+0] = 1.0;
    in[(dec_axis*step)+0] = dec_len;
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra4 = out[(ra_axis*step)+0] * RAD2DEG;
    dec4 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Make sure that RA values are in the range 0-360 (not +/-180). */
    ra1 = (ra1 < 0.0) ? (360.0 + ra1) : ra1;
    ra2 = (ra2 < 0.0) ? (360.0 + ra2) : ra2;
    ra3 = (ra3 < 0.0) ? (360.0 + ra3) : ra3;
    ra4 = (ra4 < 0.0) ? (360.0 + ra4) : ra4;

    /* Count the number of axes and compute their lengths, and the total
     * number of pixels.
     */
    for (i=0, datalen=1;  i < naxes;  i++) {
	long npix = abs(naxis[i]);
	if (npix > 1)
	    real_naxes++;
	datalen *= npix;
    }

    /* Image center. */
    if (ra3 > ra1)
	ra_cen = (ra3 - ra1) / 2.0 + ra1;
    else
	ra_cen = (ra1 - ra3) / 2.0 + ra3;
    if (dec3 > dec1)
	dec_cen = (dec3 - dec1) / 2.0 + dec1;
    else
	dec_cen = (dec1 - dec3) / 2.0 + dec3;

    /* Image scale.  We assume that pixels are square.  DEC is used to
     * avoid the cos(dec) term required to convert delta-RA to angular
     * extent on the sky.
     */
    double im_scale = ((dec3 - dec1) / dec_len);
    im_scale = im_scale * (60 * 60);     /* arcsec */


    /*
     * Output the standard image metadata.
     * ---------------------------------------------------
     */

    fprintf(fp, "\n[%s]\n", imagefile);    /* Start a new context. */

    fprintf(fp, "access_format = %s\n", "image/fits");
    long fsize = 0;  struct stat statbuf;
    if (stat(imagefile, &statbuf) != 0)
	fsize = datalen * (abs(bitpix) / 8);
    else
	fsize = statbuf.st_size;
    fprintf(fp, "access_estsize = %ld\n", fsize / 1024);

    fprintf(fp, "dataproduct_type = %s\n", real_naxes > 2 ? "cube" : "image");
    fprintf(fp, "dataset_length = %ld\n", datalen);

    /* Currently we support only a single subarray. */
    fprintf(fp, "im_nsubarrays = 1\n");

    /* Image geometry. */
    fprintf(fp, "im_naxes = %d\n", real_naxes);
    for (i=0;  i < naxes;  i++)
	fprintf(fp, "im_naxis%d = %ld\n", i+1, naxis[i]);

    /* Pixel datatype as in VOTable. */
    switch (bitpix) {
    case 8:
	fprintf(fp, "im_pixtype = %s\n", "unsignedByte");
	break;
    case 16:
	fprintf(fp, "im_pixtype = %s\n", "short");
	break;
    case 32:
	fprintf(fp, "im_pixtype = %s\n", "int");
	break;
    case -32:
	fprintf(fp, "im_pixtype = %s\n", "float");
	break;
    case -64:
	fprintf(fp, "im_pixtype = %s\n", "double");
	break;
    default:
	error(6, "invalid value for bitpix", NULL);
    }

    /* WCS axes. */
    for (i=0;  i < naxes;  i++)
	fprintf(fp, "im_wcsaxes%d = %s\n", i+1, ctype[i]);

    /* Image corners. */
    fprintf(fp, "im_ra1 = %.*g\n", DBL_DIG, ra1);
    fprintf(fp, "im_dec1 = %.*g\n", DBL_DIG, dec1);
    fprintf(fp, "im_ra2 = %.*g\n", DBL_DIG, ra2);
    fprintf(fp, "im_dec2 = %.*g\n", DBL_DIG, dec2);
    fprintf(fp, "im_ra3 = %.*g\n", DBL_DIG, ra3);
    fprintf(fp, "im_dec3 = %.*g\n", DBL_DIG, dec3);
    fprintf(fp, "im_ra4 = %.*g\n", DBL_DIG, ra4);
    fprintf(fp, "im_dec4 = %.*g\n", DBL_DIG, dec4);

    /* Image scale. */
    fprintf(fp, "im_scale = %g\n", im_scale);

    /* Image center. */
    fprintf(fp, "s_ra = %.*g\n", DBL_DIG, ra_cen);
    fprintf(fp, "s_dec = %.*g\n", DBL_DIG, dec_cen);

    /* Image field of view.  Let's use the smaller extent. */
    double ra_width = abs(ra3 - ra1) * cos(dec3 / RAD2DEG);
    double dec_width = abs(dec3 - dec1);
    fprintf(fp, "s_fov = %g\n", min(ra_width, dec_width));

    /* Image footprint (STC AstroCoordArea). */
    fprintf(fp, "s_region = polygon icrs");
    fprintf(fp, " %g %g", ra1, dec1); fprintf(fp, " %g %g", ra2, dec2);
    fprintf(fp, " %g %g", ra3, dec3); fprintf(fp, " %g %g", ra4, dec4);
    fprintf(fp, "\n");

    /* Spectral axis, if given.  This will be skipped if both the CTYPE 
     * and CUNIT are not defined, or have invalid values.
     */
    if (em_ctype && em_unit) {
	double em1 = out[(em_axis*step)+0];
	double em2 = out[(em_axis*step)+1];

	em1 = image2wave(em1, em_ctype, em_unit, &status1);
	em2 = image2wave(em2, em_ctype, em_unit, &status2);
	if (em2 > em1) {
	    double temp = em1;
	    em1 = em2;
	    em2 = temp;
	}

	if (status1 == 0 && status2 == 0) {
	    /* Spectral coverage. */
	    fprintf(fp, "em_min = %.*g\n", DBL_DIG, em1);
	    fprintf(fp, "em_max = %.*g\n", DBL_DIG, em2);

	    if (em2-em1 > EPSILOND) {
		/* Spectral resolution. */
		double em_res = (em2 - em1) / naxis[em_axis];
		double em_loc = (em2 - em1) / 2.0 + em1;
		fprintf(fp, "em_resolution = %g\n", em_res);

		/* Spectral resolving power. */
		fprintf(fp, "em_res_power = %g\n", em_loc / em_res);
	    }
	}
    }

    /* Polarization axis, if given. */
    /* to be added. */

    /* Observable axis. */
    if (bunit[0] != '\0')
	fprintf(fp, "o_unit = %s\n", bunit);
}


/* Read the image header and extract essential metadata.
 */
static AstFrameSet *
read_header(char *imagefile, int *naxes, long *naxis, int *bitpix, int *axmap,
    char ctype[][KEYLEN], char cunit[][KEYLEN], char bunit[KEYLEN],
    char comment[][COMLEN], int maxaxes) {

    AstFrameSet *wcsinfo = NULL;
    AstFitsChan *fitschan = NULL;
    fitsfile *fptr = NULL;
    int status = 0;   /* CFITSIO status value MUST be initialized to zero! */
    int nkeys = 0;
    char *header;

    if ((fits_open_file (&fptr, imagefile, READONLY, &status) != 0) ||
	(fits_hdr2str (fptr, 0, NULL, 0, &header, &nkeys, &status ) != 0)) {

	error (14, "error reading FITS header", imagefile);

    } else {
	char key[20];
	int i;

	/* Initialize output arrays. */
	memset(naxis, 0, sizeof(*naxis) * maxaxes);
	for (i=0;  i < maxaxes;  i++) {
	    ctype[i][0] = '\0';
	    cunit[i][0] = '\0';
	}

	/* Get the primary FITS header metadata. */
	fits_get_img_param(fptr, maxaxes, bitpix, naxes, naxis, &status);

	for (i=0;  i < *naxes;  i++) {
	    sprintf (key, "CTYPE%d", i + 1);
	    if (fits_read_key (fptr, TSTRING, key, ctype[i], comment[i], &status))
	        break;
	    sprintf (key, "CUNIT%d", i + 1);
	    if (fits_read_key (fptr, TSTRING, key, cunit[i], NULL, &status)) {
	        cunit[i][0] = '\0';
		status = 0;
	    }
	}

	/* Get BUNIT. */
	if (fits_read_key (fptr, TSTRING, "BUNIT", bunit, NULL, &status)) {
	    bunit[0] = '\0';
	    status = 0;
	}

	/* Compute the axis map.  This tells which image axis
	 * corresponds to the 1st or 2nd spatial axis, time axis,
	 * spectral axis, or polarization axis.  For the moment we
	 * use simple string comparison, and support for esoteric
	 * time scales is limited.
	 */
	for (i=AX_SP1;  i <= AX_POL;  i++)
	    axmap[i] = -1;

	for (i=0;  i < *naxes;  i++) {
	    if (debug)
		printf("axis %d, CTYPE = %s\n", i, ctype[i]);

	    if (strncasecmp(ctype[i],      "RA", 2) == 0)
		axmap[i] = AX_SP1;
	    else if (strncasecmp(ctype[i], "DEC", 3) == 0)
		axmap[i] = AX_SP2;
	    else if (strncasecmp(ctype[i], "GLON", 4) == 0)
		axmap[i] = AX_SP1;
	    else if (strncasecmp(ctype[i], "GLAT", 4) == 0)
		axmap[i] = AX_SP2;
	    else if (strncasecmp(ctype[i], "STOKES", 6) == 0)
		axmap[i] = AX_POL;
	    else if (strncasecmp(ctype[i], "FREQ", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "WAVE", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "ENER", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "WAVN", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "VRAD", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "VOPT", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "ZOPT", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "AWAV", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "FELO", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "VELO", 4) == 0)
		axmap[i] = AX_EM;
	    else if (strncasecmp(ctype[i], "UTC", 3) == 0)
		axmap[i] = AX_TIME;
	    else if (strncasecmp(ctype[i], "TT", 2) == 0)
		axmap[i] = AX_TIME;
	    else if (strncasecmp(ctype[i], "TAI", 3) == 0)
		axmap[i] = AX_TIME;
	    else if (strncasecmp(ctype[i], "GMT", 3) == 0)
		axmap[i] = AX_TIME;
	    else
		error (15, "unknown CTYPE value", ctype[i]);
	}

	/* Create a FitsChan and fill it with FITS header cards. */
	fitschan = astFitsChan (NULL, NULL, "");
	astPutCards (fitschan, header);

	/* Free the memory holding the concatenated header cards. */
	free (header);
	header = NULL;

	/* Read WCS information from the FitsChan. */
	wcsinfo = astRead (fitschan);
    }

    if (status == END_OF_FILE)
	status = 0; /* Reset after normal error */
    fits_close_file(fptr, &status);

    if (status || wcsinfo == NULL || !astOK) {
	fits_report_error(stderr, status); /* print any error message */
	error (16, "error reading FITS header", imagefile);
    }

    return (astSimplify(wcsinfo));
}


/* Find the given world axis in the axis map.  The axis map tells,
 * for each physical image axis, what type of world coordinate is
 * on the axis.  -1 is returned if the axis type is not found.
 */
static int
findAxis (int axis_type, int *axmap, int naxes) {
    int i;
    for (i=0;  i < naxes;  i++)
	if (axmap[i] == axis_type)
	    return (i);
    return (-1);
}


/* Convert a wavelength value in meters into the units used in the image WCS.
 * (Skip energy units for now).
static double
wave2image (double wave, char *ctype, char *unit, int *status) {
    double val=wave, scale=1.0;

    if (strncasecmp(ctype, "FREQ", 4) == 0) {
	if (strncasecmp(unit, "Hz", 2) == 0)
	    scale = 1.0;
	else if (strncasecmp(unit, "KHz", 3) == 0)
	    scale = 1.0E3;
	else if (strncasecmp(unit, "MHz", 3) == 0)
	    scale = 1.0E6;
	else if (strncasecmp(unit, "GHz", 3) == 0)
	    scale = 1.0E9;
	else
	    error (10, "unrecognized frequency unit", unit);
	val = (299792458.0 / wave) * scale;

    } else if (strncasecmp(ctype, "WAVE", 4) == 0) {
	if (strncasecmp(unit, "m", 1) == 0)
	    scale = 1.0;
	else if (strncasecmp(unit, "cm", 2) == 0)
	    scale = 1.0E2;
	else if (strncasecmp(unit, "mm", 2) == 0)
	    scale = 1.0E3;
	else if (strncasecmp(unit, "um", 2) == 0)
	    scale = 1.0E6;
	else if (strncasecmp(unit, "nm", 2) == 0)
	    scale = 1.0E9;
	else
	    error (11, "unrecognized wavelength unit", unit);
	val = wave * scale;

    } else
	*status = -1;

    return (val);
}
 */


/* Convert a wavelength value in image WCS units to meters.
 * (Skip energy units for now).
 */
static double
image2wave (double imval, char *ctype, char *unit, int *status) {
    double val=imval, scale=1.0;

    if (strncasecmp(ctype, "FREQ", 4) == 0) {
	if (strncasecmp(unit, "Hz", 2) == 0)
	    scale = 1.0;
	else if (strncasecmp(unit, "KHz", 3) == 0)
	    scale = 1.0E3;
	else if (strncasecmp(unit, "MHz", 3) == 0)
	    scale = 1.0E6;
	else if (strncasecmp(unit, "GHz", 3) == 0)
	    scale = 1.0E9;
	else
	    error (12, "unrecognized frequency unit", unit);
	val = 299792458.0 / (imval / scale);

    } else if (strncasecmp(ctype, "WAVE", 4) == 0) {
	if (strncasecmp(unit, "m", 1) == 0)
	    scale = 1.0;
	else if (strncasecmp(unit, "cm", 2) == 0)
	    scale = 1.0E2;
	else if (strncasecmp(unit, "mm", 2) == 0)
	    scale = 1.0E3;
	else if (strncasecmp(unit, "um", 2) == 0)
	    scale = 1.0E6;
	else if (strncasecmp(unit, "nm", 2) == 0)
	    scale = 1.0E9;
	else
	    error (13, "unrecognized wavelength unit", unit);
	val = imval / scale;

    } else
	*status = -1;

    return (val);
}


/*
 * ERROR - Process an error and exit the program.
 */
void error(int exit_code, char *error_message, char *tag) {
    fflush (stdout);

    if (tag != NULL && strlen(tag) > 0)
	fprintf(stderr, "ERROR %s: %s (%s)\n", program_name, error_message, tag);
    else
	fprintf(stderr, "ERROR %s: %s\n", program_name, error_message);

    exit(exit_code);
}
