(function ($) {
  // register namespace
  $.extend(true, window, {
    "cadc": {
      "vot": {
        "Viewer": Viewer,
        "CHECKBOX_SELECTOR_COLUMN_ID": "_checkbox_selector",
        "datatype": {
          "NUMERIC": "NUMERIC",
          "STRING": "STRING",
          "DATETIME": "DATETIME"
        },
        "DEFAULT_CELL_PADDING_PX": 8,
        "events": {
          "onSort": new jQuery.Event("cadcVOTV:onSort"),
          "onColumnOrderReset": new jQuery.Event("cadcVOTV:onColumnOrderReset")
        }
      }
    }
  });

  /**
   * Create a VOView object.  This is here to package everything together.
   *
   * @param targetNodeSelector  The target node selector to place the this.
   * @param options             The options object.
   * editable: true/false,
   * enableAddRow: true/false,
   * showHeaderRow: true/false,
   * enableCellNavigation: true/false,
   * asyncEditorLoading: true/false,
   * forceFitColumns: true/false,
   * explicitInitialization: true/false,
   * topPanelHeight: Number,
   * headerRowHeight: Number,
   * showTopPanel: true,
   * sortColumn: Start Date
   * sortDir: asc/desc
   * @constructor
   */
  function Viewer(targetNodeSelector, options)
  {
    var _self = this;
    var $_lengthFinder = $("#lengthFinder")
        || $("<div id='lengthFinder'></div>").appendTo($(document.body));
    this.dataView = new Slick.Data.DataView({ inlineFilters: true });
    this.grid = null;
    this.columnManager = options.columnManager ? options.columnManager : {};
    this.rowManager = options.rowManager ? options.rowManager : {};

    this.columns = [];
    // displayColumns: columns that are actually in the Grid.
    this.displayColumns = options.displayColumns ? options.displayColumns : [];
    this.resizedColumns = {};  // Columns the user has resized.
    this.columnFilters = options.columnFilters ? options.columnFilters : {};
    this.updatedColumnSelects = {};
    this.targetNodeSelector = targetNodeSelector;
    this.columnOptions = options.columnOptions ? options.columnOptions : {};
    this.options = options;
    this.options.forceFitColumns = options.columnManager
        ? options.columnManager.forceFitColumns
        : false;

    // This is the TableData for a VOTable.  Will be set on load.
    this.longestValues = {};

    this.sortcol = options.sortColumn;
    this.sortAsc = options.sortDir == "asc";

    /**
     * @param input  Object representing the input.
     *
     * One of xmlDOM or json or url is required.
     *
     * input.xmlDOM = The XML DOM Object
     * input.json = The JSON Object
     * input.csv = The CSV text
     * input.url = The URL of the input.  The Content-Type will dictate how to
     *             build it.  This is the only way to stream CSV.
     * @param completeCallback  Callback function when complete.
     * @param errorCallBack     Callback function with jqXHR, status, message
     *                    (Conforms to jQuery error callback for $.ajax calls).
     */
    function build(input, completeCallback, errorCallBack)
    {
      new cadc.vot.Builder(options.maxRowLimit,
                           input, 
                           function (voTableBuilder)
                           {
                             var hasDisplayColumns =
                                 (_self.displayColumns
                                     && (_self.displayColumns.length > 0));

                             // Set up to stream.
                             if (input.url)
                             {
                               var inputFields =
                                   input.tableMetadata.getFields();
                               
                               // Display spinner only if paging is off
                               if (!usePager())
                               {
                                  // remove any background color resulting from previous warning message
                                  if ($("#results-grid-header").prop("style"))
                                  {
                                    $("#results-grid-header").prop("style").backgroundColor = "";
                                  }

                                 // add a spinner to the header bar to indicate
                                 // streaming has begun
                                 var gridHeaderIcon = $("#grid-header-icon");
                                 if (gridHeaderIcon)
                                 {
                                   gridHeaderIcon.attr("src", "/cadcVOTV/images/PleaseWait-small.gif");
                                 }
                               }

                               /*
                                * We need to refresh columns twice; once to
                                * display something while the data is streaming,
                                * and again to update the column widths based
                                * on data.
                                *
                                * jenkinsd 2013.12.20
                                */
                               if (!hasDisplayColumns)
                               {
                                 refreshColumns(inputFields);
                               }

                               voTableBuilder.subscribe(cadc.vot.onDataLoadComplete,
                                                        function(event, args)
                               {
                                 setLongestValues(args.longestValues);
                                 resetColumnWidths();
				 
                                 // Display spinner only if paging is off
                                 if (!usePager())
                                 {
                                   var gridHeaderIcon = $("#grid-header-icon");
                                   if (gridHeaderIcon)
                                   {
                                     // clear the wait icon
                                     gridHeaderIcon.prop("src", "/cadcVOTV/images/transparent-20.png");
                                     if (options.maxRowLimit <= getDataView().getPagingInfo().totalRows)
                                     {
                                       // and display warning message if maximum row limit is reached
                                       $("#grid-header-label").text($("#grid-header-label").text() + " " + options.maxRowLimitWarning);
                                       $("#results-grid-header").prop("style").backgroundColor = "rgb(235, 235, 49)";
                                     }
                                   }
                                 }
                               });

                               clearRows();

                               // Setup the Grid and DataView to be loaded.
                               _self.init();

                               voTableBuilder.subscribe(cadc.vot.onPageAddStart,
                                                        function(event)
                                                        {
                                                          getDataView().beginUpdate();
                                                        });
                                                        
                               voTableBuilder.subscribe(cadc.vot.onPageAddEnd,
                                                        function(event)
                                                        {
                                                          getDataView().endUpdate();

                                                          // Sorting as data loads.  Not sure if this is a good idea or not.
                                                          // jenkinsd 2014.05.09
                                                          // WebRT 53730
                                                          //
                                                          sort();
                                                        });

                               voTableBuilder.subscribe(cadc.vot.onRowAdd,
                                                        function (event, row)
                                                        {
                                                          addRow(row, null);
                                                        });

                               voTableBuilder.build(
                                   voTableBuilder.buildRowData);
                             }
                             else
                             {
                               voTableBuilder.build(
                                   voTableBuilder.buildRowData);
                               _self.load(voTableBuilder.getVOTable(),
                                          !hasDisplayColumns, true);
                               _self.init();
                             }

                             if (completeCallback)
                             {
                               completeCallback();
                             }
                           }, errorCallBack);
    }

    function getTargetNodeSelector()
    {
      return _self.targetNodeSelector;
    }

    function getPagerNodeSelector()
    {
      return "#pager";
    }

    function getHeaderNodeSelector()
    {
      return "div.grid-header";
    }

    function getColumnManager()
    {
      return _self.columnManager;
    }

    function getRowManager()
    {
      return _self.rowManager;
    }

    function getColumns()
    {
      return _self.columns;
    }

    function getColumnOptions()
    {
      return _self.columnOptions;
    }

    function getOptionsForColumn(columnLabel)
    {
      return getColumnOptions()[columnLabel]
          ? getColumnOptions()[columnLabel] : {};
    }
    
    function getResizedColumns()
    {
        return _self.resizedColumns;
    }
    
    function getUpdatedColumnSelects()
    {
        return _self.updatedColumnSelects;
    }

    /**
     * Obtain whether the global fitMax or per column fitMax option has been
     * set.
     *
     * @param columnID    The column ID to check.
     */
    function isFitMax(columnID)
    {
      var columnOptions = getOptionsForColumn(columnID);

      return (columnOptions && columnOptions.fitMax) || getOptions().fitMax;
    }

    function getColumnFilters()
    {
      return _self.columnFilters;
    }

    function clearColumnFilters()
    {
      _self.columnFilters = {};
    }

    /**
     * Obtain a column from the Grid by its unique ID.
     * @param columnID    The Column ID.
     * @returns {Object} column definition.
     */
    function getGridColumn(columnID)
    {
      var existingColumnIndex = getGrid().getColumnIndex(columnID);

      var col;

      if (!isNaN(existingColumnIndex))
      {
        col = getGrid().getColumns()[existingColumnIndex];
      }
      else
      {
        col = null;
      }

      return col;
    }

    /**
     * Obtain the index of the given column ID.  Return the index, or -1 if it
     * does not exist.
     *
     * @param columnID
     * @returns {number}
     */
    function getColumnIndex(columnID)
    {
      var allCols = getColumns();

      for (var i = 0; i < allCols.length; i++)
      {
        var nextCol = allCols[i];

        if (nextCol.id == columnID)
        {
          return i;
        }
      }

      return -1;
    }

    /**
     * Obtain a column from the CADC VOTV column cache by its unique ID.
     * @param columnID    The Column ID.
     * @returns {Object} column definition.
     */
    function getColumn(columnID)
    {
      var columnIndex = getColumnIndex(columnID);

      var col;

      if (columnIndex || (columnIndex === Number(0)))
      {
        col = getColumns()[columnIndex];
      }
      else
      {
        col = null;
      }

      return col;
    }

    function addColumn(columnObject)
    {
      _self.columns.push(columnObject);
    }

    function setColumns(cols)
    {
      _self.columns = cols.slice(0);
    }

    function clearColumns()
    {
      _self.columns.length = 0;
    }

    /**
     * Add a VOTable Row.
     *
     * @param row       The cadc.vot.Row object.
     * @param rowIndex  The optional row index.
     */
    function addRow(row, rowIndex)
    {
      var dataRow = {};

      dataRow["id"] = row.getID();
      $.each(row.getCells(), function (cellIndex, cell)
      {
        var cellFieldID = cell.getField().getID();
        dataRow[cellFieldID] = cell.getValue();
      });

      if (rowIndex)
      {
        getDataView().getItems()[rowIndex] = dataRow;
      }
      else
      {
        getDataView().getItems().push(dataRow);
      }
    }

    function clearRows()
    {
      getDataView().beginUpdate();
      getDataView().setItems([]);
      getDataView().endUpdate();
    }

    function setDataView(dataViewObject)
    {
      _self.dataView = dataViewObject;
    }

    function getDataView()
    {
      return _self.dataView;
    }

    function setGrid(gridObject)
    {
      _self.grid = gridObject;
    }

    function getSelectedRows()
    {
      return getGrid().getSelectedRows();
    }

    function getRow(_index)
    {
      return getDataView().getItem(_index);
    }

    function getRows()
    {
      return getDataView().getItems();
    }

    function getGrid()
    {
      return _self.grid;
    }

    function refreshGrid()
    {
      var g = getGrid();
      g.updateRowCount();
      g.invalidateAllRows();
      g.resizeCanvas();
    }

    /**
     * Tell the Grid to sort.  This exists mainly to set an initial sort column
     * on the Grid.
     */
    function sort()
    {
      if (_self.sortcol)
      {
        var isAscending = (_self.sortAsc || (_self.sortAsc == 1));
        getGrid().setSortColumn(_self.sortcol, isAscending);

        trigger(cadc.vot.events.onSort, {
          sortCol: _self.sortcol,
          sortAsc: isAscending
        });
      }
    }

    /**
     * Set the sort column.  Here mainly for testing.
     *
     * @param _sortColumn   The column ID to use.
     */
    function setSortColumn(_sortColumn)
    {
      _self.sortcol = _sortColumn;
    }

    function getGridData()
    {
      return getDataView().getItems();
    }

    function getOptions()
    {
      return _self.options;
    }

    function setOptions(_optionsDef)
    {
      _self.options = _optionsDef;
    }

    function usePager()
    {
      return getOptions() && getOptions().pager;
    }

    function getLongestValues()
    {
      return _self.longestValues;
    }

    function getLongestValue(_columnID)
    {
      return getLongestValues()[_columnID];
    }

    function setLongestValues(_longestValues)
    {
      _self.longestValues = _longestValues;
    }

    /**
     * Get the columns that are to BE displayed.
     * @return {Array}    Array of Column objects.
     */
    function getDisplayColumns()
    {
      if (!_self.displayColumns || (_self.displayColumns.length == 0))
      {
        setDisplayColumns(getDefaultColumns().slice(0));
      }
      return _self.displayColumns;
    }

    /**
     * Get the columns that ARE CURRENTLY displayed.  Useful for saving for future
     * profile usage (i.e. restoring previous session).
     *
     * @return {Array}    Array of Column objects.
     */
    function getDisplayedColumns()
    {
      var cols = [];

      if (getGrid())
      {
        cols = getGrid().getColumns();
      }
      else
      {
        cols = [];
      }

      return cols;
    }

    function setDisplayColumns(dispCols)
    {
      _self.displayColumns = dispCols;
    }

    function getDefaultColumns()
    {
      var cols = [];
      var opts = getOptions();
      var defaultColumnIDs = opts.defaultColumnIDs;
      if (!defaultColumnIDs || (defaultColumnIDs.length == 0))
      {
        cols = getColumns().slice(0);
      }
      else
      {
        for (var colID in defaultColumnIDs)
        {
          if (defaultColumnIDs[colID])
          {
            var thisCols = getColumns();
            for (var col in thisCols)
            {
              if (thisCols[col].id == defaultColumnIDs[colID])
              {
                cols.push(thisCols[col]);
              }
            }
          }
        }
      }

      return cols;
    }

    /**
     * @param filter             The filter value as entered by the user.
     * @param value              The value to be filtered or not
     * @returns {Boolean} true if value is filtered-out by filter.
     */
    function valueFilters(filter, value)
    {
      var operator = '';
      filter = $.trim(filter);

      // determine the operator and filter value
      if (filter.indexOf('= ') == 0)
      {
        filter = filter.substring(2);
      }
      else if (filter.indexOf('=') == 0)
      {
        filter = filter.substring(1);
      }
      else if (filter.indexOf('>= ') == 0)
      {
        filter = filter.substring(3);
        operator = 'ge';
      }
      else if (filter.indexOf('>=') == 0)
      {
        filter = filter.substring(2);
        operator = 'ge';
      }
      else if (filter.indexOf('<= ') == 0)
      {
        filter = filter.substring(3);
        operator = 'le';
      }
      else if (filter.indexOf('<=') == 0)
      {
        filter = filter.substring(2);
        operator = 'le';
      }
      else if (filter.indexOf('> ') == 0)
      {
        filter = filter.substring(2);
        operator = 'gt';
      }
      else if (filter.indexOf('>') == 0)
      {
        filter = filter.substring(1);
        operator = 'gt';
      }
      else if (filter.indexOf('< ') == 0)
      {
        filter = filter.substring(2);
        operator = 'lt';
      }
      else if (filter.indexOf('<') == 0)
      {
        filter = filter.substring(1);
        operator = 'lt';
      }
      else if (filter.indexOf('..') > 0)
      {
        // filter on the range and return
        var dotIndex = filter.indexOf('..');
        var left = filter.substring(0, dotIndex);
        if ((dotIndex) + 2 < filter.length)
        {
          var right = filter.substring(dotIndex + 2);

          if (areNumbers(value, left, right))
          {
            return ((parseFloat(value) < parseFloat(left))
                || (parseFloat(value) > parseFloat(right)));
          }
          else
          {
            return ((value < left) || (value > right));
          }
        }
      }

      // act on the operator and value
      value = $.trim(value);

      var isFilterNumber = isNumber(filter);

      // Special case for those number filter expectations where the data is
      // absent.
      if (isFilterNumber
          && ((value == "") || (value == "NaN") || (value == Number.NaN)))
      {
        return true;
      }
      else if (operator === 'gt')
      {
        // greater than operator
        if (areNumbers(value, filter))
        {
          return parseFloat(value) <= parseFloat(filter);
        }
        else if (areStrings(value, filter))
        {
          return value.toUpperCase() <= filter.toUpperCase();
        }
        else
        {
          return value <= filter;
        }
      }
      else if (operator == 'lt')
      {
        // less-than operator
        if (areNumbers(value, filter))
        {
          return parseFloat(value) >= parseFloat(filter);
        }
        else if (areStrings(value, filter))
        {
          return value.toUpperCase() >= filter.toUpperCase();
        }
        else
        {
          return value >= filter;
        }
      }
      else if (operator == 'ge')
      {
        // greater-than or equals operator
        if (areNumbers(value, filter))
        {
          return parseFloat(value) < parseFloat(filter);
        }
        else if (areStrings(value, filter))
        {
          return value.toUpperCase() < filter.toUpperCase();
        }
        else
        {
          return value < filter;
        }
      }
      else if (operator == 'le')
      {
        // less-than or equals operator
        if (areNumbers(value, filter))
        {
          return parseFloat(value) > parseFloat(filter);
        }
        else if (areStrings(value, filter))
        {
          return value.toUpperCase() > filter.toUpperCase();
        }
        else
        {
          return value > filter;
        }
      }
      else
      {
        // equals operator
        if (filter.indexOf('*') > -1)
        {
          // wildcard match (Replace all instances of '*' with '.*')
          filter = filter.replace(/\*/g, ".*");

          var regex = new RegExp("^" + filter + "$", "gi");
          var result = value.match(regex);

          return (!result || result.length == 0);
        }
        else
        {
          // plain equals match
          if (areNumbers(value, filter))
          {
            return (parseFloat(value) != parseFloat(filter));
          }
          else if (areStrings(value, filter))
          {
            return (value.toUpperCase() !== filter.toUpperCase());
          }
          else
          {
            return (value !== filter);
          }
        }
      }
    }

    function isNumber(val)
    {
      return !isNaN(parseFloat(val)) && isFinite(val);
    }

    function areNumbers()
    {
      for (var i = 0; i < arguments.length; i++)
      {
        if (!isNumber(arguments[i]))
        {
          return false;
        }
      }
      return true;
    }

    function areStrings()
    {
      for (var i = 0; i < arguments.length; i++)
      {
        if (!(arguments[i].substring))
        {
          return false;
        }
      }
      return true;
    }

    /**
     * Check if this Viewer contains the given column.  Used to stop duplicate
     * checkbox columns being added.
     *
     * @return  boolean True if the viewer has the given column, false otherwise.
     */
    function hasColumn(columnDefinition)
    {
      var cols = getColumns();

      for (var col in cols)
      {
        var nextCol = cols[col];

        if (nextCol.id && (nextCol.id == columnDefinition.id))
        {
          return true;
        }
      }

      return false;
    }

    /**
     * Calculate the width of a column from its longest value.
     * @param _column     The column to calculate for.
     * @returns {number}  The integer width.
     */
    function calculateColumnWidth(_column)
    {
      var columnName = _column.name;
      var colOpts = getOptionsForColumn(columnName);
      var minWidth = columnName.length;
      var longestCalculatedWidth = getLongestValue(_column.id);
      var textWidthToUse = (longestCalculatedWidth > minWidth)
          ? longestCalculatedWidth : minWidth;

      var lengthStr = "";
      var userColumnWidth = colOpts.width;

      for (var v = 0; v < textWidthToUse; v++)
      {
        lengthStr += "_";
      }

      $_lengthFinder.addClass(_column.name);
      $_lengthFinder.text(lengthStr);

      var width = ($_lengthFinder.width() + 1);
      var colWidth = (userColumnWidth || width);

      $_lengthFinder.removeClass(_column.name);
      $_lengthFinder.empty();

      // Adjust width for cell padding.
      return colWidth + cadc.vot.DEFAULT_CELL_PADDING_PX;
    }

    /**
     * Used for resetting the force fit column widths.
     */
    function resetColumnWidths()
    {
      var allCols = getColumns();

      for (var i = 0; i < allCols.length; i++)
      {
        var col = allCols[i];
        var initialWidth = getOptionsForColumn(col.name).width;

        if (initialWidth && (initialWidth !== Number(0)))
        {
          col.width = initialWidth;
        }
        else
        {
          setColumnWidth(col);
        }
      }

      var gridColumns = getGrid().getColumns();
      var dupGridColumns = [];
      var totalWidth = 0;

      // Handle the visible columns
      for (var j = 0; j < gridColumns.length; j++)
      {
        var gridColumn = gridColumns[j];
        var existingColumn = getColumn(gridColumn.id);

        // Update the equivalent in the grid, if it's there.
        if (existingColumn)
        {
          gridColumn.width = existingColumn.width;
        }

        totalWidth += gridColumn.width;

        dupGridColumns.push(gridColumn);
      }

      getGrid().setColumns(dupGridColumns);

      if (totalWidth > 0)
      {
        $(getTargetNodeSelector()).css("width", (totalWidth + 15) + "px");

        if (usePager())
        {
          $(getPagerNodeSelector()).css("width", (totalWidth + 15) + "px");
        }

        $(getHeaderNodeSelector()).css("width", (totalWidth + 15) + "px");
      }

      _self.refreshGrid();
    }

    function setColumnWidth(_columnDefinition)
    {
      // Do not calculate with checkbox column.
      if ((_columnDefinition.id != cadc.vot.CHECKBOX_SELECTOR_COLUMN_ID)
          && (isFitMax(_columnDefinition.id) || getOptions().forceFitColumns))
      {
        _columnDefinition.width = calculateColumnWidth(_columnDefinition);
      }
    }

    /**
     * Initialize this VOViewer.
     */
    function init()
    {
      var dataView = getDataView();
      var forceFitMax = (getColumnManager().forceFitColumns
                             && getColumnManager().forceFitColumnMode
          && (getColumnManager().forceFitColumnMode
          == "max"));
      var checkboxSelector;
      var enableSelection = !getOptions().enableSelection
          || getOptions().enableSelection == true;

      if ((typeof CADC !== 'undefined')
          && (typeof CADC.CheckboxSelectColumn !== 'undefined'))
      {
        checkboxSelector = new CADC.CheckboxSelectColumn({
                                                           cssClass: "slick-cell-checkboxsel",
                                                           width: 55,
                                                           headerCssClass: "slick-header-column-checkboxsel"
                                                         });
      }
      else if (Slick.CheckboxSelectColumn)
      {
        checkboxSelector = new Slick.CheckboxSelectColumn({
                                                            cssClass: "slick-cell-checkboxsel",
                                                            width: 55,
                                                            headerCssClass: "slick-header-column-checkboxsel"
                                                          });
      }
      else
      {
        checkboxSelector = null;
      }

      if (checkboxSelector && enableSelection) 
      {
        var checkboxColumn = checkboxSelector.getColumnDefinition();
        var colsToCheck = (getDisplayColumns().length == 0)
            ? getColumns() : getDisplayColumns();

        var checkboxColumnIndex = -1;

        $.each(colsToCheck, function (index, val)
        {
          if (checkboxColumn.id == val.id)
          {
            checkboxColumnIndex = index;
          }
        });

        if (checkboxColumnIndex < 0)
        {
          getColumns().splice(0, 0, checkboxColumn);
          getDisplayColumns().splice(0, 0, checkboxColumn);
        }
        else
        {
          getColumns()[checkboxColumnIndex] = checkboxColumn;
          getDisplayColumns()[checkboxColumnIndex] = checkboxColumn;
        }
      }

      getOptions().defaultFormatter = function (row, cell, value, columnDef,
                                                dataContext)
      {
        var returnValue;

        if (value == null)
        {
          returnValue = "";
        }
        else
        {
          returnValue = value.toString().replace(/&/g, "&amp;").
              replace(/</g, "&lt;").replace(/>/g, "&gt;");
        }

        return "<span class='cellValue " + columnDef.id
                   + "' title='" + returnValue + "'>" + returnValue + "</span>";
      };

      var grid = new Slick.Grid(getTargetNodeSelector(),
                                dataView, getDisplayColumns(),
                                getOptions());
      var rowSelectionModel;

      if (checkboxSelector)
      {
        if ((typeof CADC !== 'undefined')
            && (typeof CADC.RowSelectionModel !== 'undefined'))
        {
          rowSelectionModel =
            new CADC.RowSelectionModel({
                                         selectActiveRow: getOptions().selectActiveRow,
                                         selectClickedRow: getOptions().selectClickedRow
                                       });
        }
        else if (Slick.RowSelectionModel)
        {
          rowSelectionModel =
            new Slick.RowSelectionModel({
                                          selectActiveRow: getOptions().selectActiveRow
                                        });
        }
        else
        {
          rowSelectionModel = null;
        }

        if (rowSelectionModel)
        {
          grid.setSelectionModel(rowSelectionModel);
        }

        grid.registerPlugin(checkboxSelector);
      }
      else
      {
        rowSelectionModel = null;
      }

      if (usePager())
      {
        var pager = new Slick.Controls.Pager(dataView, grid,
                                             $(getPagerNodeSelector()));
      }
      else
      {
        // Use the Grid header otherwise.
        var gridHeaderLabel = $("#grid-header-label");

        if (gridHeaderLabel)
        {
          dataView.onPagingInfoChanged.subscribe(function (e, pagingInfo)
                                                 {
                                                   gridHeaderLabel.text("Showing " + pagingInfo.totalRows
                                                                            + " rows (" + getGridData().length
                                                                            + " before filtering).");
                                                 });
        }
      }

      var columnPickerConfig = getColumnManager().picker;

      if (columnPickerConfig)
      {
        var columnPicker;
        var pickerStyle = columnPickerConfig.style;

        if (pickerStyle == "header")
        {
          columnPicker = new Slick.Controls.ColumnPicker(getColumns(),
                                                         grid, getOptions());
          if (forceFitMax)
          {
            columnPicker.onColumnAddOrRemove.subscribe(resetColumnWidths);
          }
        }
        else if (pickerStyle == "tooltip")
        {
          columnPicker = new Slick.Controls.PanelTooltipColumnPicker(getColumns(),
                                                                     grid,
                                                                     columnPickerConfig.panel,
                                                                     columnPickerConfig.tooltipOptions,
                                                                     columnPickerConfig.options);

          if (forceFitMax)
          {
            columnPicker.onSort.subscribe(resetColumnWidths);
            columnPicker.onResetColumnOrder.subscribe(resetColumnWidths);
            columnPicker.onShowAllColumns.subscribe(resetColumnWidths);
            columnPicker.onSortAlphabetically.subscribe(resetColumnWidths);
          }

          columnPicker.onColumnAddOrRemove.subscribe(function(e, args)
                                                     {
                                                       if (rowSelectionModel)
                                                       {
                                                         // Refresh.
                                                         rowSelectionModel.refreshSelectedRanges();
                                                       }
                                                     });
        }
        else
        {
          columnPicker = null;
        }
      }

      if (columnPicker)
      {
        columnPicker.onResetColumnOrder.subscribe(function()
                                                  {
                                                    // Clear the hash.
                                                    parent.location.hash = '';
                                                    trigger(cadc.vot.events.onColumnOrderReset, null);
                                                  });
      }

      if (forceFitMax)
      {
        var totalWidth = 0;
        var gridColumns = grid.getColumns();

        for (var c in gridColumns)
        {
          var nextCol = gridColumns[c];
          totalWidth += nextCol.width;
        }

        $(getTargetNodeSelector()).css("width", totalWidth + "px");

        if (usePager())
        {
          $(getPagerNodeSelector()).css("width", totalWidth + "px");
        }

        $(getHeaderNodeSelector()).css("width", totalWidth + "px");
        grid.resizeCanvas();
      }

      // move the filter panel defined in a hidden div into grid top panel
      $("#inlineFilterPanel").appendTo(grid.getTopPanel()).show();

      grid.onCellChange.subscribe(function (e, args)
                                  {
                                    dataView.updateItem(args.item.id, args.item);
                                  });

      grid.onKeyDown.subscribe(function (e)
                               {
                                 // select all rows on ctrl-a
                                 if ((e.which != 65) || !e.ctrlKey)
                                 {
                                   return false;
                                 }

                                 var rows = [];
                                 for (var i = 0; i < dataView.getLength(); i++)
                                 {
                                   rows.push(i);
                                 }

                                 grid.setSelectedRows(rows);
                                 e.preventDefault();

                                 return true;
                               });

      /**
       * Tell the dataview to do the comparison.
       */
      var doGridSort = function()
      {
        var isnumeric = _self.getColumn(_self.sortcol).datatype.isNumeric();
        var comparer =
            new cadc.vot.Comparer(_self.sortcol, isnumeric);

        // using native sort with comparer
        // preferred method but can be very slow in IE
        // with huge datasets
        dataView.sort(comparer.compare, _self.sortAsc);
        dataView.refresh();
      };

      /**
       * Handle the local sort events.  These events are fired for the initial
       * sort when the Grid is loaded, if any.
       *
       * WebRT 53730
       */
      subscribe(cadc.vot.events.onSort, function(eventData, args)
      {
        _self.sortAsc = args.sortAsc;
        _self.sortcol = args.sortCol;

        doGridSort();
      });

      /**
       * Handle the Grid sorts.
       */
      grid.onSort.subscribe(function (e, args)
                            {
                              _self.sortAsc = args.sortAsc;
                              _self.sortcol = args.sortCol.field;

                              doGridSort();
                            });

      // wire up model events to drive the grid
      dataView.onRowCountChanged.subscribe(function (e, args)
                                           {
                                             grid.updateRowCount();
                                           });

      if (getRowManager().onRowRendered)
      {
        grid.onRowsRendered.subscribe(function(e, args)
                                      {
                                        $.each(args.renderedRowIndexes,
                                               function(rowIndexIndex, rowIndex)
                                               {
                                                 var $rowItem =
                                                     dataView.getItem(rowIndex);
                                                 getRowManager().onRowRendered($rowItem);
                                               });
                                      });
      }


      dataView.onRowsChanged.subscribe(function (e, args)
                                       {
                                         grid.invalidateRows(args.rows);
                                         grid.render();
                                       });

      dataView.onPagingInfoChanged.subscribe(function (e, pagingInfo)
                                             {
                                               var isLastPage =
                                                   (pagingInfo.pageNum == pagingInfo.totalPages - 1);
                                               var enableAddRow =
                                                   (isLastPage || pagingInfo.pageSize == 0);
                                               var options = grid.getOptions();

                                               if (options.enableAddRow != enableAddRow)
                                               {
                                                 grid.setOptions({enableAddRow: enableAddRow});
                                               }
                                             });

      $(window).resize(function ()
                       {
                         grid.resizeCanvas();
                       });

      $("#btnSelectRows").click(function ()
                                {
                                  if (!Slick.GlobalEditorLock.commitCurrentEdit())
                                  {
                                    return;
                                  }

                                  var rows = [];
                                  for (var i = 0;
                                       (i < 10) && (i < dataView.getLength()); i++)
                                  {
                                    rows.push(i);
                                  }

                                  grid.setSelectedRows(rows);
                                });


      var columnFilters = getColumnFilters();

      $(grid.getHeaderRow()).delegate(":input", "change keyup",
                                      function (e)
                                      {
                                        var $thisInput = $(this);
                                        var columnId =
                                            $thisInput.data("columnId");
                                        if (columnId)
                                        {
                                          columnFilters[columnId] =
                                          $.trim($thisInput.val());
                                          dataView.refresh();
                                        }
                                      });

      grid.onHeaderRowCellRendered.subscribe(function (e, args)
                                             {
                                               $(args.node).empty();

                                               // Display the label for the checkbox column filter row.
                                               if (checkboxSelector
                                                   && (args.column.id == checkboxSelector.getColumnDefinition().id))
                                               {
                                                 $("<div class='filter-boxes-label' "
                                                       + "title='Enter values into the boxes to further filter results.'>Filter:</div>").
                                                     appendTo(args.node);
                                               }

                                               // Do not display for the checkbox column.
                                               else if (args.column.filterable)
                                               {
                                                 // Allow for overrides per column.
                                                 if (args.column.filterable == false)
                                                 {
                                                   $("<span class=\"empty\"></span>").
                                                       appendTo(args.node);
                                                 }
                                                 else
                                                 {
                                                   var datatype =
                                                       args.column.datatype;
                                                   var tooltipTitle;

                                                   if (datatype.isNumeric())
                                                   {
                                                     tooltipTitle = "Number: 10 or >=10 or 10..20 for a range , ! to negate";
                                                   }
                                                   else
                                                   {
                                                     tooltipTitle = "String: abc (exact match) or *ab*c* , ! to negate";
                                                   }

                                                   $("<input type='text'>")
                                                       .data("columnId", args.column.id)
                                                       .val(columnFilters[args.column.id])
                                                       .prop("title", tooltipTitle)
                                                       .prop("id", args.column.utype + "_filter")
                                                       .appendTo(args.node);
                                                 }
                                               }
                                             });

      if (Slick.Plugins && Slick.Plugins.UnitSelection)
      {
        var unitSelectionPlugin = new Slick.Plugins.UnitSelection();

        // Extend the filter row to include the pulldown menu.
        unitSelectionPlugin.onUnitChange.subscribe(function (e, args)
                                                   {
                                                     if (columnPicker.updateColumnData)
                                                     {
                                                       columnPicker.updateColumnData(
                                                           args.column.id,
                                                           "unitValue",
                                                           args.unitValue);
                                                     }
                                                     // track select changes.
                                                     _self.updatedColumnSelects[args.column.id] = args.unitValue;

                                                     // Invalidate to force column
                                                     // reformatting.
                                                     grid.invalidate();
                                                   });

        grid.registerPlugin(unitSelectionPlugin);
      }
      
      // Track the width of resized columns.
      grid.onColumnsResized.subscribe(function(e, args) {
        var columns = args.grid.getColumns();
        $.each(columns, function(index, column) {
            if (column.width !== column.previousWidth) {
                getResizedColumns[column.id] = column.width;
                return false;
            }
        });
      });

      setDataView(dataView);
      setGrid(grid);

      if (forceFitMax)
      {
        resetColumnWidths();
      }

      sort();
    }

    /**
     * Load a fresh copy into this this.  This assumes first time load.
     *
     * @param voTable         The built VOTable.
     * @param _refreshColumns  Whether to refresh the columns (true/false).
     * @param _refreshData     Whether to refresh the data (true/false).
     */
    function load(voTable, _refreshColumns, _refreshData)
    {
      // Use the first Table of the first Resource only.
      var resource = voTable.getResources()[0];

      if (!resource)
      {
        throw new Error("No resource available.");
      }

      var table = resource.getTables()[0];

      if (!table)
      {
        throw new Error("No table available.");
      }

      setLongestValues(table.getTableData().getLongestValues());

      if (_refreshColumns)
      {
        refreshColumns(table.getFields());
      }

      if (_refreshData)
      {
        refreshData(table);
      }
    }

    /**
     * Update the columns in the grid with the cached ones.  This method exists
     * to make use of the fitMax option.
     */
    function refreshGridColumns()
    {
      var allCols = getGrid().getColumns();
      var visibleColumns = getGrid().getColumns();
      var dupColumns = visibleColumns.slice(0);

      for (var i = 0; i < allCols.length; i++)
      {
        var nextCol = allCols[i];
        var nextVisibleColumn = visibleColumns[i];

        if (nextCol)
        {
          if (getOptionsForColumn(nextCol.id).fitMax)
          {
            nextCol.width = calculateColumnWidth(nextCol);
            dupColumns[i].width = nextCol.width;
          }
        }
      }

      getGrid().setColumns(dupColumns);
    }

    /**
     * Refresh this Viewer's columns.
     *
     * WARNING: This will clear ALL of the columns, including the checkbox
     * selector column.  Generally, this method will only be called to
     * initialize the columns from the init() method, or when first building
     * the viewer.
     *
     * @param _fields   A Table in the VOTable.
     */
    function refreshColumns(_fields)
    {
      clearColumns();
      var columnManager = getColumnManager();

      $.each(_fields, function (fieldIndex, field)
      {
        var fieldKey = field.getID();
        var colOpts = getOptionsForColumn(fieldKey);
        var cssClass = colOpts.cssClass;
        var datatype = field.getDatatype();
        var filterable = columnManager.filterable
            && (((colOpts.filterable != undefined) && (colOpts.filterable != null))
            ? colOpts.filterable : columnManager.filterable);

        // We're extending the column properties a little here.
        var columnObject =
        {
          id: fieldKey,
          name: field.getName(),
          field: fieldKey,
          formatter: colOpts.formatter,
          asyncPostRender: colOpts.asyncFormatter,
          cssClass: cssClass,
          description: field.getDescription(),
          resizable: getColumnManager().resizable,
          sortable: colOpts.sortable ? colOpts.sortable : true,

          // VOTable attributes.
          unit: field.getUnit(),
          utype: field.getUType(),
          filterable: filterable
        };

        // Default is to be sortable.
        columnObject.sortable =
          ((colOpts.sortable != null) && (colOpts.sortable != undefined))
              ? colOpts.sortable : true;

        if (datatype)
        {
          columnObject.datatype = datatype;
        }

        columnObject.header = colOpts.header;

        if (colOpts.width)
        {
          columnObject.width = colOpts.width;
        }
        else if (columnManager.forceFitColumns || isFitMax(columnObject.id))
        {
          columnObject.width = calculateColumnWidth(columnObject);
        }

        addColumn(columnObject);
      });
    }

    /**
     * Function for the search filter to run.  This is meant to be in the
     * context of the dataView, so 'this' will refer to the current instance of
     * the data view.
     *
     * @param item      Filter item.
     * @param args      columnFilters - columnFilter object.
     *                  grid - grid object.
     *                  doFilter - filter method.
     * @returns {boolean}
     */
    function searchFilter(item, args)
    {
      var filters = args.columnFilters;
      var grid = args.grid;

      for (var columnId in filters)
      {
        var filterValue = filters[columnId];
        if ((columnId !== undefined) && (filterValue !== ""))
        {
          var columnIndex = grid.getColumnIndex(columnId);
          var column = grid.getColumns()[columnIndex];
          var cellValue = item[column.field];
          var rowID = item["id"];
          var columnFormatter = column.formatter;

          // Reformatting the cell value could potentially be quite expensive!
          // This may require some re-thinking.
          // jenkinsd 2013.04.30
          if (columnFormatter)
          {
            var row = grid.getData().getIdxById(rowID);
            var formattedCellValue =
                columnFormatter(row, columnIndex, cellValue, column, item);

            cellValue = formattedCellValue && $(formattedCellValue).text
                ? $(formattedCellValue).text() : formattedCellValue;
          }

          filterValue = $.trim(filterValue);
          var negate = filterValue.indexOf("!") == 0;

          if (negate)
          {
            filterValue = filterValue.substring(1);
          }

          var filterOut = args.doFilter(filterValue, cellValue);

          if ((!negate && filterOut) || (!filterOut && negate))
          {
            return false;
          }
        }
      }

      return true;
    }

    /**
     * Clean refresh of the data rows.
     *
     * @param table   A Table element from a VOTable.
     */
    function refreshData(table)
    {
      clearRows();

      // Make a copy of the array so as not to disturb the original.
      var allRows = table.getTableData().getRows();

      $.each(allRows, function (rowIndex, row)
      {
        addRow(row, rowIndex);
      });
    }

    function render()
    {
      var dataView = getDataView();
      var g = getGrid();

      // initialize the model after all the events have been hooked up
      dataView.beginUpdate();
      dataView.setFilterArgs({
                               columnFilters: getColumnFilters(),
                               grid: g,
                               doFilter: valueFilters
                             });
      dataView.setFilter(searchFilter);
      dataView.endUpdate();

      if (g.getSelectionModel())
      {
        // If you don't want the items that are not visible (due to being filtered out
        // or being on a different page) to stay selected, pass 'false' to the second arg
        dataView.syncGridSelection(g, true);
      }

      var gridContainer = $(getTargetNodeSelector());

      if (gridContainer.resizable && getOptions().gridResizable)
      {
        gridContainer.resizable();
      }

      g.init();
    }

    /**
     * Fire an event.  Taken from the slick.grid Object.
     *
     * @param _event       The Event to fire.
     * @param _args        Arguments to the event.
     * @returns {*}       The event notification result.
     */
    function trigger(_event, _args)
    {
      var args = _args || {};
      args.application = _self;

      return $(_self).trigger(_event, _args);
    }

    /**
     * Subscribe to one of this form's events.
     *
     * @param _event      Event object.
     * @param __handler   Handler function.
     */
    function subscribe(_event, __handler)
    {
      $(_self).on(_event.type, __handler);
    }

    $.extend(this,
             {
               "init": init,
               "build": build,
               "render": render,
               "load": load,
               "areNumbers": areNumbers,
               "areStrings": areStrings,
               "getOptions": getOptions,
               "setOptions": setOptions,
               "refreshGrid": refreshGrid,
               "getGrid": getGrid,
               "getDataView": getDataView,
               "getColumn": getGridColumn,
               "getColumns": getColumns,
               "setColumns": setColumns,
               "clearColumns": clearColumns,
               "getSelectedRows": getSelectedRows,
               "getRow": getRow,
               "getRows": getRows,
               "addRow": addRow,
               "clearColumnFilters": clearColumnFilters,
               "getColumnFilters": getColumnFilters,
               "setDisplayColumns": setDisplayColumns,
               "getDisplayedColumns": getDisplayedColumns,
               "valueFilters": valueFilters,
               "searchFilter": searchFilter,
               "setSortColumn": setSortColumn,
               "getResizedColumns": getResizedColumns,
               "getUpdatedColumnSelects": getUpdatedColumnSelects,

               // Event subscription
               "subscribe": subscribe
             });
  }
})(jQuery);
