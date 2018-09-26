(function ($) {
  // register namespace
  $.extend(true, window, {
    "cadc": {
      "web": {
        "util": {
          "URI": URI
        }
      }
    }
  });

  function URI(uri)
  {
    var _self = this;
    this.uri = uri;
    this.uriComponents = parse();
    this.query = parseQuery();

    function getURI()
    {
      return _self.uri;
    }

    // This function creates a new anchor element and uses location
    // properties (inherent) to get the desired URL data. Some String
    // operations are used (to normalize results across browsers).
    function parse()
    {
      var parser = /^(?:([^:\/?\#]+):)?(?:\/\/([^\/?\#]*))?([^?\#]*)(?:\?([^\#]*))?(?:\#(.*))?/;
      var parsedURI = getURI().match(parser);
      var components = {};

      components.scheme = parsedURI[1] || "";
      components.host = parsedURI[2] || "";
      components.path = parsedURI[3] || "";
      components.query = parsedURI[4] || "";
      components.hash = parsedURI[5] || "";
      components.file = ((components.path
                          && components.path.match(/\/([^\/?#]+)$/i)) || [,''])[1];

      return components;
    }

    /**
     * Obtain the relative URI for this URI.  Meaning, obtain the host-less
     * version of this URI, to avoid cross-domain constraint issues.
     *
     * @return  Relative URI, or empty string if none available.
     */
    function getRelativeURI()
    {
      var relativeURI = getPath();
      var queryString = getQueryString();
      var hashString = getHash();

      if (queryString)
      {
        relativeURI += "?" + queryString;
      }

      if (hashString)
      {
        relativeURI += "#" + hashString;
      }

      return relativeURI;
    }

    function getURIComponents()
    {
      return _self.uriComponents;
    }

    /**
     * Create an Object from the query string.
     *
     * @returns {{Object}}
     */
    function parseQuery()
    {
      var nvpair = {};
      var qs = getURIComponents().query;
      if (qs.trim())
      {
        var pairs = (qs != "") ? qs.split("&") : [];
        $.each(pairs, function(i, v)
        {
          var pair = v.split('=');
          var queryKey = pair[0];
          var keyValues = nvpair[queryKey] || [];

          // TODO - Is it a good idea to always decode this?
          keyValues.push(decodeURIComponent(pair[1]));

          nvpair[queryKey] = keyValues;
        });
      }
      return nvpair;
    }

    function getQueryString()
    {
      return getURIComponents().query;
    }

    function getHash()
    {
      return getURIComponents().hash;
    }

    function getPath()
    {
      return getURIComponents().path;
    }

    function getPathItems()
    {
      var splitItems = getPath().split("/");

      if ((splitItems.length > 0) && (splitItems[0] == ""))
      {
        // If the path starts with a '/', then the first item will be an empty
        // string, so get rid of it.
        splitItems.splice(0, 1);
        return splitItems;
      }
      else
      {
        return splitItems;
      }
    }

    function getFile()
    {
      return getURIComponents().file;
    }

    function getHost()
    {
      return getURIComponents().host;
    }
    
    function getScheme()
    {
        return getURIComponents().scheme;
    }

    /**
     * Key -> value representation of the query string.  Assumes one value per
     * key.
     *
     * @returns {{Object}}
     * @deprecated  Use getQuery() object instead.
     */
    function getQueryStringObject()
    {
      var nvpair = {};
      var qs = getURIComponents().query.replace('?', '');
      var pairs = qs.split('&');

      $.each(pairs, function(i, v)
      {
        var pair = v.split('=');

        nvpair[pair[0]] = pair[1];
      });

      return nvpair;
    }

    /**
     * Return a key => array values pair.
     *
     * @returns {{String}}  String key with array of values.
     */
    function getQuery()
    {
      return _self.query;
    }

    /**
     * Return a single value for a key.
     *
     * @returns {{Object}}  value or null.
     */
    function getQueryValue(_key)
    {
      var queryItemArray = getQueryValues(_key);
      var val;

      if (queryItemArray.length > 0)
      {
        val = queryItemArray[0];
      }
      else
      {
        val = null;
      }

      return val;
    }

    /**
     * Return an array of values for the given key.
     *
     * @returns {{Object}}  Array of items, or empty array.
     */
    function getQueryValues(_key)
    {
      var queryItemArray = getQuery()[_key];
      var val;

      if (queryItemArray && (queryItemArray.length > 0))
      {
        val = queryItemArray;
      }
      else
      {
        val = [];
      }

      return val;
    }

    $.extend(this,
             {
               "getQuery": getQuery,
               "getQueryString": getQueryString,
               "getQueryStringObject": getQueryStringObject,
               "getQueryValue": getQueryValue,
               "getQueryValues": getQueryValues,
               "getPath": getPath,
               "getHost": getHost,
               "getPathItems": getPathItems,
               "getFile": getFile,
               "getURI": getURI,
               "getRelativeURI": getRelativeURI,
               "getHash": getHash,
               "getScheme": getScheme
             });
  }
})(jQuery);