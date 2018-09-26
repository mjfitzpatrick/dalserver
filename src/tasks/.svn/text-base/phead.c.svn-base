#include <string.h>
#include <stdio.h>
#include "fitsio.h"
#include "ast.h"

int main(int argc, char *argv[])
{
    fitsfile *fptr;         /* FITS file pointer, defined in fitsio.h */
    AstFitsChan *fitschan;
    AstFrameSet *wcsinfo, *simpler;
    int status = 0;   /* CFITSIO status value MUST be initialized to zero! */
    int single = 0, hdupos, nkeys, verbose=0;
    void transform();
    char *header;
    char *fname;

    if (strcmp (argv[1], "-v") == 0) {
	verbose = 1;
	fname = argv[2];
    } else
	fname = argv[1];

    if (!fits_open_file(&fptr, fname, READONLY, &status)) {
      fits_get_hdu_num(fptr, &hdupos);  /* Get the current HDU position */

      /* List only a single header if a specific extension was given */ 
      if (hdupos != 1 || strchr(argv[1], '[')) single = 1;

      for (; !status; hdupos++)  /* Main loop through each extension */ {
        printf("Header listing for HDU #%d:\n", hdupos);

	if (fits_hdr2str (fptr, 0, NULL, 0, &header, &nkeys, &status ) )
	    printf(" Error getting header\n");
	else {
	    astBegin;

	    /* Create a FitsChan and fill it with FITS header cards. */
	    fitschan = astFitsChan( NULL, NULL, "" );
	    astPutCards( fitschan, header );

	    /* Free the memory holding the concatenated header cards. */
	    free (header);
	    header = NULL;

	    /* Read WCS information from the FitsChan. */
	    wcsinfo = astRead( fitschan );
	    if (wcsinfo == NULL)
		break;

	    /* Print the WCS info. */
	    simpler = astSimplify (wcsinfo);
	    if (simpler != NULL) {
		if (verbose)
		    astShow (simpler);
	    } else
		printf ("astSimplify failed\n");

	    /* Test coordinate transformation. */
	    (void) transform (fptr, simpler);

	    astEnd;
	}

        if (single) break;  /* quit if only listing a single header */
        fits_movrel_hdu(fptr, 1, NULL, &status);  /* try to move to next HDU */
      }

      if (status == END_OF_FILE)
	  status = 0; /* Reset after normal error */

      fits_close_file(fptr, &status);
    }

    if (status)
	fits_report_error(stderr, status); /* print any error message */

    return (status);
}


/*
 * Transform - test an N-D coordinate transform.
 */
void transform (fitsfile *fptr, AstFrameSet *wcs) {
    int status = 0;   /* CFITSIO status value MUST be initialized to zero! */
    double in[64], out[64];
    int bitpix, naxis, i;
    int imdim=4, npts=2, istep=2, ostep=2;
    char ctype[8][20], cunit[8][20], key[20];
    char comment[8][64];
    void printvec();
    long naxes[10];

    bzero ((void *)naxes, sizeof(naxes));
    bzero ((void *)ctype, sizeof(ctype));
    bzero ((void *)cunit, sizeof(cunit));
    fits_get_img_param(fptr, 10, &bitpix, &naxis, naxes, &status);

    imdim = naxis;  /* should be naxes! */
    for (i=1;  i <= imdim;  i++) {
      sprintf (key, "CTYPE%d", i);
      if (fits_read_key (fptr, TSTRING, key, ctype[i], comment[i], &status))
        break;
      sprintf (key, "CUNIT%d", i);
      if (fits_read_key (fptr, TSTRING, key, cunit[i], comment[i], &status))
        break;
    }

    printf ("naxes=%d, naxis=[%ld,%ld,%ld,%ld]\n", imdim,
        naxes[0], naxes[1], naxes[2], naxes[3]);
    printf ("ctypes: [%s,%s,%s,%s]   --  ",
        ctype[1], ctype[2], ctype[3], ctype[4]);
    printf ("cunits: [%s,%s,%s,%s]\n",
        cunit[1], cunit[2], cunit[3], cunit[4]);

    bzero ((void *)in, sizeof(in));
    bzero ((void *)out, sizeof(out));
    astSetI (wcs, "report", 1);

    /* Forward transform. */
    astTranN (wcs, npts, imdim, istep, in, 1, imdim, ostep, out);
    printvec (out, 16, 1);

    /* Reverse transform. */
    astTranN (wcs, npts, imdim, ostep, out, 0, imdim, istep, in);
    printvec (in, 16, 1);

    /* Forward transform, using output from above. */
    astTranN (wcs, npts, imdim, istep, in, 1, imdim, ostep, out);
    printvec (out, 16, 1);
}


/* Utility to dump a vector. */
void printvec (double *vec, int n, int extraline) {
    int i;

    printf ("Output:  ");
    for (i=0;  i < n;  i++)
	printf ("%g ", vec[i]);
    printf ("\n");

    if (extraline)
	printf ("\n");
}

