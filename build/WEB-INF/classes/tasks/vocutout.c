#include <stdlib.h>
#include <unistd.h>
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

/* Command line options. */
#define	RA		21	/* RA of region center, ICRS */
#define	DEC		22	/* DEC of region center, ICRS */
#define	WIDTH		23	/* Angular width of region, deg */
#define	HEIGHT		24	/* Angular height of region, deg */
#define	WAVELO		25	/* Start of spectral band, meters */
#define	WAVEHI		26	/* End of spectral band, meters */
#define	TIMELO		27	/* Start of time band, MJD */
#define	TIMEHI		28	/* End of time band, MJD */
#define	POLSTATES	29	/* List of polarization states */
#define	SECTION		30	/* User-supplied image section */

#define abs(x)		((x)<0 ? -(x):(x))
#define min(x,y)	((x)<(y)?x:y)
#define max(x,y)	((x)>(y)?x:y)
#define nint(x)		((int)((x)+0.5))

/* Global params shared by all functions. */
static int debug = 0;
const char *program_name=NULL;
static char *pubdid=NULL, *dir=NULL, *polstates=NULL;
static int spatial_filter=0, spectral_filter=0, time_filter=0, pol_filter=0;
static double wavelo=0.0, wavehi=0.0, timelo=0.0, timehi=0.0;
static double ra=0.0, dec=0.0, width=0.0, height=0.0;
static int filter_term=0, pixel_term=0;
static char *imsection=NULL;

static AstFrameSet *read_header();
static char *compute_metadata();
static void return_metadata();
static char *extract_image();
static char *getKeyword();
static int findAxis();
static double wave2image();
static double image2wave();
static void error();

/*
 * VOCUTOUT - Cutout regions of multi-parameter space from a FITS image.
 *
 * The initial version supports only the spatial and spectral axes,
 * although support for time and polarization would be easy to add later.
 * The Starlink AST library is used for WCS processing; CFITSIO is used
 * for FITS file access.
 */
int
main(int argc, char *argv[]) {
    program_name = argv[0];
    int metadata=0, extract=0, force=0;;
    char *mdfile = NULL, *imagefile=NULL;
    int ch;

    /* Command line arguments. */
    static char keyopts[] = "Dmxp:l:d:F";
    static struct option longopts[] = {
	{ "debug",	no_argument,		NULL,	'D' },
	{ "metadata",	no_argument,		NULL,	'm' },
	{ "extract",	no_argument,		NULL,	'x' },
	{ "pubdid",	required_argument,	NULL,	'p' },
	{ "mdfile",	required_argument,	NULL,	'l' },
	{ "dir",	required_argument,	NULL,	'd' },
	{ "force",	no_argument,		NULL,	'F' },
	{ "ra",		required_argument,	NULL,	RA  },
	{ "dec",	required_argument,	NULL,	DEC },
	{ "width",	required_argument,	NULL,	WIDTH },
	{ "height",	required_argument,	NULL,	HEIGHT },
	{ "wavelo",	required_argument,	NULL,	WAVELO },
	{ "wavehi",	required_argument,	NULL,	WAVEHI },
	{ "timelo",	required_argument,	NULL,	TIMELO },
	{ "timehi",	required_argument,	NULL,	TIMEHI },
	{ "polstates",	required_argument,	NULL,	POLSTATES },
	{ "section",	required_argument,	NULL,	SECTION },
	{ NULL,		0,			NULL,	0 },
    };

    /* Process command line options. */
    while ((ch = getopt_long(argc, argv, keyopts, longopts, NULL)) != -1) {
	char *endptr;

	switch (ch) {
	case 'D':
	    debug++;
	    break;
	case 'm':
	    metadata++;
	    break;
	case 'x':
	    extract++;
	    break;
	case 'p':
	    pubdid = optarg;
	    break;
	case 'l':
	    mdfile = optarg;
	    break;
	case 'd':
	    dir = optarg;
	    break;
	case 'F':
	    force++;
	    break;
	case RA:
	    ra = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(RA, "invalid RA value", optarg);
	    spatial_filter++;
	    filter_term = 1;
	    break;
	case DEC:
	    dec = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(DEC, "invalid DEC value", optarg);
	    spatial_filter++;
	    filter_term = 1;
	    break;
	case WIDTH:
	    width = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(WIDTH, "invalid WIDTH value", optarg);
	    spatial_filter++;
	    break;
	case HEIGHT:
	    height = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(HEIGHT, "invalid HEIGHT value", optarg);
	    spatial_filter++;
	    break;
	case WAVELO:
	    wavelo = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(WAVELO, "invalid WAVELO value", optarg);
	    spectral_filter++;
	    filter_term = 1;
	    break;
	case WAVEHI:
	    wavehi = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(WAVEHI, "invalid WAVEHI value", optarg);
	    spectral_filter++;
	    filter_term = 1;
	    break;
	case TIMELO:
	    timelo = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(TIMELO, "invalid TIMELO value", optarg);
	    time_filter++;
	    filter_term = 1;
	    break;
	case TIMEHI:
	    timehi = strtod(optarg, &endptr);
	    if (endptr == optarg)
		error(TIMEHI, "invalid TIMEHI value", optarg);
	    time_filter++;
	    filter_term = 1;
	    break;
	case POLSTATES:
	    polstates = optarg;
	    pol_filter++;
	    filter_term = 1;
	    break;
	case SECTION:
	    imsection = optarg;
	    pixel_term = 1;
	    break;

	default:
	    error(1, "unknown option", optarg);
	}
    }

    argc -= optind;
    argv += optind;

    /* Get the pathname of the image to be processed. */
    imagefile = argv[0];

    if (debug) {
	printf("prog=%s, mdfile=%s, imagefile=%s\n",
	    program_name, mdfile, imagefile);
	printf("metadata=%d, extract=%d\n", metadata, extract);
    }

    /* If no precomputed metadata for the virtual image is referenced
     * (by pointing to a mdfile), then we need to analyze the input image
     * and specfified filter parameters to determine how to process the
     * image.  The creation metadata for the virtual image will be saved
     * to a mdfile and may be used later to instantiate the image.
     */
    if (mdfile == NULL) {
	if (imagefile == NULL)
	    error(2, "no imagefile specified", NULL);
	else if ((mdfile = compute_metadata (imagefile)) == NULL)
	    error(3, "computation of virtual image failed", imagefile);
    }

    /* If requested, return metadata for the virtual image to the client. */
    if (metadata)
	return_metadata(mdfile, stdout);

    /* If requested, compute the cutout image and return access information
     * to the client.  Created images are saved as files in the staging area
     * (rather than for example streaming them back dynamically), to permit
     * caching to optimize repeated accesses, and to facilitate creation of
     * images via asynchronous batch processing.
     */
    if (extract) {
	char *fname;
	if ((fname = extract_image(mdfile, force)) == NULL)
	    error(4, "image creation failed", mdfile);
	fprintf(stdout, "image = %s\n", fname);
    }

    exit (0);
}


/*
 * Compute the metadata for a virtual image to be generated from the
 * referenced static image file.  The input parameters specify two terms
 * of the image access model, the filter (world-cutout) term, and the
 * pixel-space image section term.  The filter term specifies the bounds
 * in multi-parameter space of the region to be extracted from the image.
 * The image section term applies to the virtual image resulting from 
 * application of the filter term.  Both terms are optional; the entire
 * image is returned if both are absent.
 *
 * Metadata is saved to a MDFILE, and the name of this file is returned as
 * the function argument.
 */
static char *
compute_metadata(char *imagefile) {
    long naxis[MAXAXES], datalen;
    int real_naxes, axmap[MAXAXES], imdim, naxes, bitpix, i;
    double ra_cen, dec_cen, ra1, dec1, ra2, dec2, ra3, dec3, ra4, dec4;
    int cutout1[MAXAXES], cutout2[MAXAXES], npts=2, step=2;
    char ctype[MAXAXES][KEYLEN]; char cunit[MAXAXES][KEYLEN];
    double in[MAXAXES*2], out[MAXAXES*2];
    char comment[MAXAXES][COMLEN];
    int status1=0, status2=0;
    AstFrameSet *wcs;
    char *mdfile;

    astBegin;

    /* Read the image geometry and WCS. */
    if ((wcs = read_header(imagefile,
	    &naxes, naxis, &bitpix, axmap, ctype, cunit, comment, MAXAXES)) == NULL) {
	error(5, "cannot read metadata for image", imagefile);
    }

    /* Compute the world coordinates of the full image by taking the forward
     * transform of the full pixel array.  Due to the way array dimensioning
     * is used AST wants the coordinates of the two points to be interleaved, e.g.,
     * with a stepsize of 2, pt1-val1, pt2-val1, pt1-val2, pt2-val2, etc.
     */
    for (i=0, imdim=naxes;  i < imdim;  i++) {
	in[(i*step)+0] = 1.0;
	in[(i*step)+1] = naxis[i];
    }
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);

    /* Apply each given constraint filter.  The image is assumed to be
     * in approximately the same region of parameter space as the requested
     * cutout region; this should have been ensured by the first stage of
     * selection.  This code is hardly rigorous at this point and some
     * assumptions are made to simplify the code.
     */
    int ra_axis = findAxis(AX_SP1, axmap, naxes);
    int dec_axis = findAxis(AX_SP2, axmap, naxes);
    int em_axis = findAxis(AX_EM, axmap, naxes);
    int time_axis = findAxis(AX_TIME, axmap, naxes);
    int pol_axis = findAxis(AX_POL, axmap, naxes);

    char *em_ctype = ctype[em_axis];
    if (em_ctype[0] == '\0')
	em_ctype = NULL;
    char *em_unit = cunit[em_axis];
    if (em_unit[0] == '\0')
	em_unit = NULL;

    /* Disable filtering on an axis if we don't have enough WCS information. */
    if (ra_axis < 0 || dec_axis < 0)
	spatial_filter = 0;
    if (em_axis < 0)
	spectral_filter = 0;
    if (time_axis < 0)
	time_filter = 0;
    if (pol_axis < 0)
	pol_filter = 0;

    if (debug) {
	// RA, DEC at image edge.
	double rs = out[(ra_axis*step)+0] * RAD2DEG;
	double re = out[(ra_axis*step)+1] * RAD2DEG;
	double ds = out[(dec_axis*step)+0] * RAD2DEG;
	double de = out[(dec_axis*step)+1] * RAD2DEG;
	printf ("Spatial:  ra_start %.*g ra_end %.*g dec_start %.*g dec_end %.*g\n",
	    DBL_DIG, rs, DBL_DIG, re, DBL_DIG, ds, DBL_DIG, de);
    }

    if (spatial_filter) {
	/* AST expresses spatial coords in radians.  ICRS is assumed here,
	 * and is enforced in read_header().  If either axis of the image
	 * is flipped the axis will be flipped to the standard orientation
	 * in the generated cutout.  If the image is rotated the cutout
	 * will also be rotated, and will only approximate the requested
	 * region.
	 */
	if (height <= 0.0)
	    height = width;

	double ra1 = ra - (width / 2.0);
	double ra2 = ra + (width / 2.0);
	double dec1 = dec - (height / 2.0);
	double dec2 = dec + (height / 2.0);

	if (debug) {
	    printf ("Spatial:  ra %.*g dec %.*g size %.*g %.*g\n",
		DBL_DIG, ra, DBL_DIG, dec, DBL_DIG, width, DBL_DIG, height);
	    printf ("       :  r1 %.*g r2 %.*g d1 %.*g d2 %.*g\n",
		DBL_DIG, ra1, DBL_DIG, ra2, DBL_DIG, dec1, DBL_DIG, dec2);
	}

	out[(ra_axis*step)+0] = ra1 / RAD2DEG;
	out[(ra_axis*step)+1] = ra2 / RAD2DEG;
	out[(dec_axis*step)+0] = dec1 / RAD2DEG;
	out[(dec_axis*step)+1] = dec2 / RAD2DEG;
    }

    if (spectral_filter) {
	/* Only WAVE and FREQ image coords are currently supported, in a
	 * variety of units.  If the computation fails, due to a sufficiently
	 * wierd image or whatever, we disable filtering of the axis and
	 * merely return the entire spectral axis.
	 */
	double em1 = wave2image(wavelo, em_ctype, em_unit, &status1);
	double em2 = wave2image(wavehi, em_ctype, em_unit, &status2);
	if (status1 == 0 && status2 == 0) {
	    out[(em_axis*step)+0] = em1;
	    out[(em_axis*step)+1] = em2;
	}

	if (debug) {
	    printf ("Spectral:  wavelo %.*g em1 %.*g wavehi %.*g em2 %.*g\n",
		DBL_DIG, wavelo, DBL_DIG, em1, DBL_DIG, wavehi, DBL_DIG, em2);
	}
    }

    if (time_filter) {
	/* ignore; not yet implemented. */
    }
    if (pol_filter) {
	/* ignore; not yet implemented. */
    }

    /* We now have the bounding world coordinates of the (unclipped) cutout
     * region.  Axes which were not filtered are unchanged.  We perform the
     * reverse transformation to get back to image pixel coordinates.
     */
    astTranN (wcs, npts, imdim, step, out, 0, imdim, step, in);

    /* Clip the cutout region, which may extend beyond the bounds of the
     * target image, to the physical image pixel matrix of the target image.
     * If any projected axis lies completely outside the target image the
     * cutout has no coverage in the ROI (it must have coverage in all dimensions),
     * and is rejected.
     *
     * The final region in pixel coordinates is specified by cutout1,2[n] 
     * where N is the axis number (0-4) and cutout1-2 are the starting and
     * ending pixel values for that axis.
     */
    for (i=0;  i < naxes;  i++) {
	double v1 = in[(i*step)+0];
	double v2 = in[(i*step)+1];

	// Check that we have some coverage.
	if (v1 > naxis[i] || v2 < 1.0) {
	    char region[20];  sprintf(region, "axis%d: %s", i+1, ctype[i]);
	    if (debug)
		printf("v1=%g axlim=%ld v2=%g, axlim=1\n", v1, naxis[i], v2);
	    error (7, "no coverage in selected region", region);
	}

	if (debug)
	    printf("Cutout: axis %d: %f %f\n", i, v1, v2);

	in[(i*step)+0] = cutout1[i] = max(1, min(naxis[i], nint(v1)));
	in[(i*step)+1] = cutout2[i] = max(1, min(naxis[i], nint(v2)));

	if (debug)
	    printf("Cutout: clip %d: %d %d\n", i, cutout1[i], cutout2[i]);
    }

    /* Do a final forward transform back to World coordinates to define
     * the physical coverage of the clipped cutout region.
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
     */ 

    /* Compute RA,DEC in degrees of image corner 1,1. */
    in[(ra_axis*step)+0] = cutout1[ra_axis];
    in[(dec_axis*step)+0] = cutout1[dec_axis];
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra1 = out[(ra_axis*step)+0] * RAD2DEG;
    dec1 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Compute RA,DEC in degrees of image corner 2,2. */
    in[(ra_axis*step)+0] = cutout2[ra_axis];
    in[(dec_axis*step)+0] = cutout1[dec_axis];
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra2 = out[(ra_axis*step)+0] * RAD2DEG;
    dec2 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Compute RA,DEC in degrees of image corner 3,3. */
    in[(ra_axis*step)+0] = cutout2[ra_axis];
    in[(dec_axis*step)+0] = cutout2[dec_axis];
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra3 = out[(ra_axis*step)+0] * RAD2DEG;
    dec3 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Compute RA,DEC in degrees of image corner 4,4. */
    in[(ra_axis*step)+0] = cutout1[ra_axis];
    in[(dec_axis*step)+0] = cutout2[dec_axis];
    astTranN (wcs, npts, imdim, step, in, 1, imdim, step, out);
    if (!astOK)
	error(6, "cannot read metadata for image", imagefile);
    ra4 = out[(ra_axis*step)+0] * RAD2DEG;
    dec4 = out[(dec_axis*step)+0] * RAD2DEG;

    /* Make sure that RA values are in the range 0-360 (not +/-180).
     * Can also be done this way:  double pt1[2]; astNorm(wcs, pt1);
     */
    ra1 = (ra1 < 0.0) ? (360.0 + ra1) : ra1;
    ra2 = (ra2 < 0.0) ? (360.0 + ra2) : ra2;
    ra3 = (ra3 < 0.0) ? (360.0 + ra3) : ra3;
    ra4 = (ra4 < 0.0) ? (360.0 + ra4) : ra4;

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
    double im_scale = ((dec3 - dec1) / dec_len);
    im_scale = im_scale * (60 * 60);
     */

    /* Metadata for a virtual image is saved to a file in the staging
     * area, and may be referenced later to create the described virtual
     * image (as when metadata computed for a virtual image in a query
     * response is later used to create and retrieve the virtual image).
     * Saving the metadata in a file is a simple way to do this for now.
     * A more scalable and persistent solution would be to create a DBMS
     * record describing the virtual image, and reference it externally
     * with a PubDID.
     */
    long cutaxis[MAXAXES];
    FILE *fp = NULL;
    int fd = -1;
    long npix;

    /* Create the MDFILE (metadata link storage file). */
    if (dir == NULL)
	error(8, "root directory of staging area not specified", NULL);

    mdfile = (char *)malloc((size_t)256);
    sprintf(mdfile, "%s/image-XXXXXX", dir);
    if ((fd = mkstemp(mdfile)) < 0)
	error(9, "cannot create metadata link file", mdfile);
    else
	fp = fdopen(fd, "r+");

    /* Save the MDFILE filename, minus the staging directory prefix. */
    fprintf(fp, "# Virtual image definition file.\n\n");
    fprintf(fp, "MDFILE = %s\n", mdfile+strlen(dir)+1);

    /* Save the filename of the archive image. */
    fprintf(fp, "image = %s\n", imagefile);


    /* ---- FILTER TERM ---- */
    fprintf(fp, "\n[filter]\n\n");

    if (filter_term) {
	fprintf(fp, "filter_term = true\n");

	/* Record the filter term required to extract the cutout region. */
	switch (naxes) {
	    case 2:
		fprintf(fp, "cutout = [%d:%d,%d:%d]\n",
		    cutout1[0], cutout2[0], cutout1[1], cutout2[1]);
		break;
	    case 3:
		fprintf(fp, "cutout = [%d:%d,%d:%d,%d:%d]\n",
		    cutout1[0], cutout2[0], cutout1[1], cutout2[1],
		    cutout1[2], cutout2[2]);
		break;
	    case 4:
		fprintf(fp, "cutout = [%d:%d,%d:%d,%d:%d,%d:%d]\n",
		    cutout1[0], cutout2[0], cutout1[1], cutout2[1],
		    cutout1[2], cutout2[2], cutout1[3], cutout2[3]);
		break;
	    default:
		fprintf(fp, "cutout = [*]\n");
		break;
	}

	/* Count the number of axes and compute their lengths. */
	for (i=0, real_naxes=0, datalen=1;  i < naxes;  i++) {
	    int v1 = cutout1[i]; int v2 = cutout2[i];
	    if (v2 > v1)
		npix = v2 - v1 + 1;
	    else
		npix = v1 - v2 + 1;

	    if (npix > 1) {
		cutaxis[real_naxes] = npix;
		real_naxes++;
	    }
	    datalen *= npix;
	}

	/* A single pixel is an image with a single axis of length 1. */
	fprintf(fp, "im_naxes = %d\n", max(1, real_naxes));
	switch (real_naxes) {
	    case 1:
		fprintf(fp, "im_naxis = %ld\n", cutaxis[0]);
		break;
	    case 2:
		fprintf(fp, "im_naxis = %ld %ld\n", cutaxis[0], cutaxis[1]);
		break;
	    case 3:
		fprintf(fp, "im_naxis = %ld %ld %ld\n",
		    cutaxis[0], cutaxis[1], cutaxis[2]);
		break;
	    case 4:
		fprintf(fp, "im_naxis = %ld %ld %ld %ld\n",
		    cutaxis[0], cutaxis[1], cutaxis[2], cutaxis[3]);
		break;
	    default:
		fprintf(fp, "im_naxis = ****\n");
	}

	fprintf(fp, "access_estsize = %ld\n", (datalen * (abs(bitpix) / 8) / 1024));
	fprintf(fp, "dataset_length = %ld\n", datalen);
	fprintf(fp, "obs_creation_type = cutout\n");

	if (spatial_filter) {
	    /* Image corners.
	     * [Does not change in a cutout, omitted.]
	     *
	    fprintf(fp, "im_ra1 = %.*g\n", DBL_DIG, ra1);
	    fprintf(fp, "im_dec1 = %.*g\n", DBL_DIG, dec1);
	    fprintf(fp, "im_ra2 = %.*g\n", DBL_DIG, ra2);
	    fprintf(fp, "im_dec2 = %.*g\n", DBL_DIG, dec2);
	    fprintf(fp, "im_ra3 = %.*g\n", DBL_DIG, ra3);
	    fprintf(fp, "im_dec3 = %.*g\n", DBL_DIG, dec3);
	    fprintf(fp, "im_ra4 = %.*g\n", DBL_DIG, ra4);
	    fprintf(fp, "im_dec4 = %.*g\n", DBL_DIG, dec4);
	     */

	    /* Image scale.
	     * [Does not change in a cutout, omitted.]
	     *
	    fprintf(fp, "im_scale = %g\n", im_scale);
	     */

	    /* Image center. */
	    fprintf(fp, "s_ra = %.*g\n", DBL_DIG, ra_cen);
	    fprintf(fp, "s_dec = %.*g\n", DBL_DIG, dec_cen);

	    /* Image field of view.  Let's use the smaller extent. */
	    double ra_width = abs(ra3 - ra1) * cos(dec3 / RAD2DEG);
	    double dec_width = abs(dec3 - dec1);
	    fprintf(fp, "s_fov = %g\n", min(ra_width, dec_width));

	    /* Image footprint (STC AstroCoordArea). */
	    fprintf(fp, "s_region = polygon icrs");
	    fprintf(fp, " %.*g %.*g", DBL_DIG, ra1, DBL_DIG, dec1);
	    fprintf(fp, " %.*g %.*g", DBL_DIG, ra2, DBL_DIG, dec2);
	    fprintf(fp, " %.*g %.*g", DBL_DIG, ra3, DBL_DIG, dec3);
	    fprintf(fp, " %.*g %.*g", DBL_DIG, ra4, DBL_DIG, dec4);
	    fprintf(fp, "\n");
	}

	if (spectral_filter) {
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

		    if ((em2-em1) > EPSILOND) {
			/* Spectral resolution. */
			double em_res = (em2 - em1) / naxis[em_axis];
			double em_loc = (em2 - em1) / 2.0 + em1;
			fprintf(fp, "em_resolution = %g\n", em_res);

			/* Spectral resolving power. */
			fprintf(fp, "em_res_power = %g\n", em_loc / em_res);
		    }
		}
	    }
	}
    } else
	fprintf(fp, "filter_term = false\n");

    /* ---- WCS TERM ---- */
    fprintf(fp, "\n[wcs]\n\n");
    fprintf(fp, "wcs_term = false\n");

    /* ---- PIXEL TERM ---- */
    fprintf(fp, "\n[pixel]\n\n");

    if (pixel_term) {
	fprintf(fp, "pixel_term = true\n");
	fprintf(fp, "section = %s\n", imsection);
    } else
	fprintf(fp, "pixel_term = false\n");

    /* ---- FUNCTION TERM ---- */
    fprintf(fp, "\n[function]\n\n");
    fprintf(fp, "function_term = false\n");

    fprintf(fp, "END\n");
    fclose(fp);
    astEnd;

    return (mdfile);
}


/* Find the given world axis in the axis map.  The axis map tells,
 * for each physical image axis, what type of world coordinate is
 * on the axis.
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
 */
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


/* Read the image header and extract essential metadata.
 */
static AstFrameSet *
read_header(char *imagefile, int *naxes, long *naxis, int *bitpix, int *axmap,
    char ctype[][KEYLEN], char cunit[][KEYLEN], char comment[][COMLEN], int maxaxes) {

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

	/* Create a FitsChan and fill it with FITS header cards. */
	fitschan = astFitsChan (NULL, NULL, "");
	astPutCards (fitschan, header);

	/* Read WCS information from the FitsChan. */
	wcsinfo = astRead (fitschan);
	int wcsdim = astGetI (wcsinfo, "Naxes");

	/* Initialize output arrays. */
	memset(naxis, 0, sizeof(*naxis) * maxaxes);
	for (i=0;  i < maxaxes;  i++) {
	    ctype[i][0] = '\0';
	    cunit[i][0] = '\0';
	}

	/* Get the primary FITS header metadata. */
	fits_get_img_param(fptr, maxaxes, bitpix, naxes, naxis, &status);

	/* Since we are working with the WCS here, we want WCSDIM not NAXES */
	*naxes = max(*naxes, wcsdim);

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

	/* Free the memory holding the concatenated header cards. */
	free (header);
	header = NULL;
    }

    if (status == END_OF_FILE)
	status = 0; /* Reset after normal error */
    fits_close_file(fptr, &status);

    if (status || wcsinfo == NULL || !astOK) {
	fits_report_error(stderr, status); /* print any error message */
	error (16, "error reading FITS header", imagefile);
    }

    return (wcsinfo);
}


/* Copy the MDFILE content to the indicated output stream.
 */
static void
return_metadata(char *mdfile, FILE *out) {
    char buf[256];
    FILE *fp;

    if ((fp = fopen(mdfile, "r")) == NULL)
	error(17, "cannot open metadata storage file", mdfile);
    while (fgets(buf, 256, fp) != NULL)
	fputs(buf, out);
}


/* Extract the image cutout specified in MDFILE.  We don't need
 * most of the metadata, only the image file name and the image
 * section, specified in pixel coordinates, required to extract
 * the filter term or cutout region.
 *
 * The MDFILE records the information needed to generate the
 * image filename.  If the image does not exist it is created
 * and the filename returned.  If the image exists we merely
 * return the filename.  If the flag "force" is set the image
 * is regenerated.
 *
 * The filename of the image associated with an MDFILE is created
 * by merely appending ".fits" to the MDFILE filename.
 */
static char *
extract_image(char *mdfile, int force) {
    static char tempimage[256], newimage[256], mdpath[256];
    char *cutout=NULL, *section=NULL, *sval=NULL;
    char imageref[256], *imagefile=NULL;
    int stat;

    /* Generate the imagefile pathname. */
    if (dir == NULL)
	error(18, "root directory of staging area not specified", NULL);
    sprintf (mdpath, "%s/%s", dir, mdfile);
    sprintf (newimage, "%s/%s.fits", dir, mdfile);
    sprintf (tempimage, "%s/%s-temp.fits", dir, mdfile);

    /* Create the image if it does not already exist, or if the
     * "force" flag is set.
     */
    if ((access (newimage, F_OK) < 0) || force) {
	unlink (newimage);

	fitsfile *infptr, *outfptr;
	int status = 0, ii = 1;

	/* Get the image pathname. */
	if ((sval = getKeyword (mdpath, "image")) != NULL)
	    imagefile = strdup(sval);

	/* --- Filter Term ---- */

	sval = getKeyword (mdpath, "filter_term");
	if (sval != NULL && strcmp(sval,"true") == 0) {

	    /* Apply the filter term.  This amounts to a simple cutout, e.g.,
	     * "image[cutout]".
	     */
	    if ((sval = getKeyword (mdpath, "cutout")) != NULL)
		cutout = strdup(sval);
	    if (imagefile == NULL || cutout == NULL)
		error (19, "cannot identify imagefile", mdfile);

	    if (imagefile[0] == '/')
		sprintf (imageref, "%s%s", imagefile, cutout);
	    else
		sprintf (imageref, "%s/%s%s", dir, imagefile, cutout);

	    /* Open the input image section for the filter term. */
	    if (fits_open_file(&infptr, imageref, READONLY, &status) != 0)
		error (20, "cannot open imagefile", imageref);

	    /* Create the output file and copy the data from the specified
	     * image section for the filter term.  This should automatically
	     * update the WCS to reflect the smaller image coverage (copying
	     * the entire image is a special case).
	     */
	    if (!fits_create_file(&outfptr, newimage, &status)) {
		/* Copy every HDU until we get an error */
		while (!fits_movabs_hdu(infptr, ii++, NULL, &status))
		    fits_copy_hdu(infptr, outfptr, 0, &status);
	 
		/* Reset status after normal error */
		if (status == END_OF_FILE)
		    status = 0;

		fits_close_file(outfptr,  &status);
	    }

	    fits_close_file(infptr, &status);

	    /* If error occured, print out error message */
	    if (status) {
		fits_report_error(stderr, status);
		error (21, "cannot create image cutout", imageref);
	    }

	    /* Apply the Pixel term if any to "newimage". */
	    imagefile = newimage;
	}

	/* --- Pixel Term ---- */

	sval = getKeyword (mdpath, "pixel_term");
	if (sval != NULL && strcmp(sval,"true") == 0) {

	    /* Apply the pixel term.  This is a pixel space operation manipulating
	     * the pixel matrix.  Currently this is just another image section, but
	     * this time it is a client-defined section.  It is applied after the
	     * filter and WCS terms if any, otherwise it is applied to the original
	     * input image.
	     */

	    if ((sval = getKeyword (mdpath, "section")) != NULL)
		section = strdup(sval);
	    if (newimage == NULL || section == NULL)
		error (19, "cannot apply pixel term", mdfile);

	    if (imagefile[0] == '/')
		sprintf (imageref, "%s%s", imagefile, section);
	    else
		sprintf (imageref, "%s/%s%s", dir, imagefile, section);

	    /* Open the input image section for the filter term. */
	    status = 0;  ii = 1;
	    if (fits_open_file(&infptr, imageref, READONLY, &status) != 0)
		error (20, "cannot open imagefile", imageref);

	    /* Create the output file and copy the data from the specified
	     * image section for the filter term.  This should automatically
	     * update the WCS to reflect the smaller image coverage (copying
	     * the entire image is a special case).
	     */
	    if (!fits_create_file(&outfptr, tempimage, &status)) {
		/* Copy every HDU until we get an error */
		while (!fits_movabs_hdu(infptr, ii++, NULL, &status))
		    fits_copy_hdu(infptr, outfptr, 0, &status);
	 
		/* Reset status after normal error */
		if (status == END_OF_FILE)
		    status = 0;

		fits_close_file(outfptr,  &status);
	    }

	    fits_close_file(infptr, &status);

	    /* If error occured, print out error message */
	    if (status) {
		fits_report_error(stderr, status);
		error (21, "cannot create image cutout", imageref);
	    }

	    stat = unlink (newimage);
	    stat = rename (tempimage, newimage);
	    if (stat != 0)
		error (22, "error renaming temporary image", imageref);
	}
    }

    return (newimage);
}


/* Small utility to read a keyword from the MDFILE (or any other
 * keyword=value formatted file.  The returned string if overwritten
 * in each call.
 */
static char *
getKeyword (char *file, char *name) {
    char token[64], *ip, *op;
    static char buf[256];
    FILE *fp;

    if ((fp = fopen(file, "r")) == NULL)
	error(22, "cannot open file", file);

    while (fgets(buf, 256, fp) != NULL) {
	for (ip=buf, op=token;  (ip-buf) < 63 && !isspace(*ip);  )
	    *op++ = *ip++;
	*op++ = '\0';

	if (strcasecmp (token, name) == 0) {
	    while (*ip && (isspace(*ip) || *ip == '='))
		ip++;
	    for (op=ip;  *op && *op != '\n';  op++)
		;
	    *op = '\0';

	    fclose (fp);
	    return (ip);
	}
    }

    fclose (fp);
    return (NULL);
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
