package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.client.search.blast.BlastSearchDataProvider;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResult;
import org.jbei.ice.lib.shared.dto.search.SearchResults;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * Presenter for searches
 *
 * @author Hector Plahar
 */
public class SearchPresenter extends AbstractPresenter {

    private enum Mode {
        BLAST, SEARCH
    }

    private final ISearchView display;
    private final SearchDataProvider dataProvider;
    private final SearchDataProvider webDataProvider;
    private final BlastSearchDataProvider blastProvider;
    private final SearchModel model;
    private final SearchResultsTable table;
    private final SearchResultsTable webResults;
    private final BlastResultsTable blastTable;
    private Mode mode;
    private SearchQuery lastQuery;

    public SearchPresenter(RegistryServiceAsync rpcService, HandlerManager eventBus, ISearchView view,
            ServiceDelegate<EntryContext> contextDelegate) {
        super(rpcService, eventBus);
        this.display = view;

        table = new SearchResultsTable(createContext(true, false, false, contextDelegate));
        webResults = new WebResultsTable(createContext(false, true, false, contextDelegate));
        blastTable = new BlastResultsTable(createContext(false, false, true, contextDelegate));

        // hide the results table
        dataProvider = new SearchDataProvider(table, rpcService, eventBus, false);
        webDataProvider = new SearchDataProvider(webResults, rpcService, eventBus, true);
        blastProvider = new BlastSearchDataProvider(blastTable, rpcService);
        model = new SearchModel(rpcService, eventBus);
        getWebOfRegistrySettings();
        addSearchHandlers();
    }

    private ServiceDelegate<SearchResult> createContext(final boolean search, final boolean web,
            final boolean blast, final ServiceDelegate<EntryContext> contextDelegate) {
        return new ServiceDelegate<SearchResult>() {
            @Override
            public void execute(SearchResult searchResult) {
                EntryContext context = new EntryContext(EntryContext.Type.SEARCH);
                if (search)
                    context.setNav(dataProvider);
                else if (web)
                    context.setNav(webDataProvider);
                else if (blast)
                    context.setNav(blastProvider);

                context.setId(searchResult.getEntryInfo().getId());
                context.setRecordId(searchResult.getEntryInfo().getRecordId());
                context.setPartnerUrl(searchResult.getWebPartnerURL());
                contextDelegate.execute(context);
            }
        };
    }

    public void addSearchHandlers() {
        display.setLocalSearchHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                search(lastQuery);
            }
        });

        display.setWebSearchHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                webSearch(lastQuery);
            }
        });
    }

    public void getWebOfRegistrySettings() {
        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.isWebOfRegistriesEnabled(callback);
            }

            @Override
            public void onSuccess(Boolean show) {
                display.showWebOfRegistryOptions(show);
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    public void addTableSelectionModelChangeHandler(Handler handler) {
        this.table.getSelectionModel().addSelectionChangeHandler(handler);
    }

    public Set<SearchResult> getResultSelectedSet() {
        return this.table.getSelectionModel().getSelectedSet();
    }

    public void search(SearchQuery searchQuery) {
        if (searchQuery == null)
            return;

        lastQuery = searchQuery;
        model.performSearch(searchQuery, false, new SearchCallback(false));
        if (searchQuery.hasBlastQuery()) {
            // show blast table loading
            searchQuery.getParameters().setSortField(ColumnField.BIT_SCORE);
            blastProvider.updateRowCount(0, false);
            display.setBlastVisibility(blastTable, true);
            blastTable.setVisibleRangeAndClearData(blastTable.getVisibleRange(), false);
        } else {
            // regular search
            searchQuery.getParameters().setSortField(ColumnField.RELEVANCE);
            dataProvider.updateRowCount(0, false);
            display.setSearchVisibility(table, true);
            table.setVisibleRangeAndClearData(table.getVisibleRange(), false);
        }
    }

    public void webSearch(SearchQuery searchQuery) {
        if (searchQuery == null)
            return;

        model.performSearch(searchQuery, true, new SearchCallback(true));
        if (searchQuery.hasBlastQuery()) {
            // show blast table loading
            blastProvider.updateRowCount(0, false);
            display.setBlastVisibility(blastTable, true);
            blastTable.setVisibleRangeAndClearData(blastTable.getVisibleRange(), false);
        } else {
            // regular search
            webDataProvider.updateRowCount(0, false);
            display.setSearchVisibility(webResults, true);
            webResults.setVisibleRangeAndClearData(webResults.getVisibleRange(), false);
        }
    }

    public Set<Long> getEntrySet() {
        switch (mode) {
            case SEARCH:
            default:
//                if (table.getSelectionModel().isAllSelected()) {
//                    return dataProvider.getData();
//                }
                return table.getSelectedEntrySet();

            case BLAST:
                return blastTable.getSelectedEntrySet();
        }
    }

    public ISearchView getView() {
        return this.display;
    }

    //
    // inner class
    //
    private class SearchCallback extends Callback<SearchResults> {

        private final boolean webSearch;

        public SearchCallback(boolean webSearch) {
            this.webSearch = webSearch;
        }

        @Override
        public void onSuccess(SearchResults searchResults) {
            if (searchResults.getQuery().hasBlastQuery()) {
                blastProvider.setBlastData(searchResults.getResults());
                mode = Mode.BLAST;
                return;
            }

            if (webSearch) {
                webDataProvider.setSearchData(searchResults);
            } else
                dataProvider.setSearchData(searchResults);
            mode = Mode.SEARCH;
        }

        public void onFailure() {
            if (mode == Mode.BLAST)
                blastProvider.setBlastData(new LinkedList<SearchResult>());
            else {
                if (webSearch)
                    webDataProvider.setSearchData(null);
                else
                    dataProvider.setSearchData(null);
            }
        }
    }

    /**
     * Results table for showing information on search results from a web of registry search
     */
    private class WebResultsTable extends SearchResultsTable {

        public WebResultsTable(ServiceDelegate<SearchResult> delegate) {
            super(delegate);
        }

        @Override
        protected ArrayList<DataTableColumn<SearchResult, ?>> createColumns(ServiceDelegate<SearchResult> delegate) {
            ArrayList<DataTableColumn<SearchResult, ?>> columns = new ArrayList<DataTableColumn<SearchResult, ?>>();
            columns.add(addScoreColumn(false));
            columns.add(super.addTypeColumn(false));
            columns.add(addPartIdColumn(delegate, false, 120, com.google.gwt.dom.client.Style.Unit.PX));
            columns.add(super.addNameColumn(120, com.google.gwt.dom.client.Style.Unit.PX));
            columns.add(addSummaryColumn());
            columns.add(addWebPartnerName());
            columns.add(addNameColumn(120, com.google.gwt.dom.client.Style.Unit.PX));
            super.addHasAttachmentColumn();
            super.addHasSequenceColumn();
            columns.add(super.addCreatedColumn(false));
            return columns;
        }

        protected DataTableColumn<SearchResult, SafeHtml> addWebPartnerName() {
            SafeHtmlCell htmlCell = new SafeHtmlCell();
            DataTableColumn<SearchResult, SafeHtml> partner =
                    new DataTableColumn<SearchResult, SafeHtml>(htmlCell, ColumnField.REGISTRY_NAME) {

                        @Override
                        public SafeHtml getValue(SearchResult object) {
                            String projectName = object.getWebPartnerName();
                            String projectURI = object.getWebPartnerURL();
                            if (projectName == null && projectURI == null)
                                return SafeHtmlUtils.EMPTY_SAFE_HTML;

                            if (projectURI == null)
                                return SafeHtmlUtils.fromSafeConstant("<i>" + projectName + "</i>");

                            String name = (projectName == null || projectName.isEmpty()) ? projectURI : projectName;
                            return SafeHtmlUtils.fromSafeConstant(
                                    "<a target=\"_blank\" href=\"" + projectURI + "\">" + name + "</a>&nbsp;<i class=\""
                                            + FAIconType.EXTERNAL_LINK.getStyleName() + " opacity_hover\"></i>");
                        }
                    };

            this.setColumnWidth(partner, 150, Unit.PX);
            this.addColumn(partner, "Registry");
            return partner;
        }

        @Override
        protected DataTableColumn<SearchResult, SafeHtml> addNameColumn(final double width, Unit unit) {
            DataTableColumn<SearchResult, SafeHtml> nameColumn =
                    new DataTableColumn<SearchResult, SafeHtml>(new SafeHtmlCell(), ColumnField.NAME) {

                        @Override
                        public SafeHtml getValue(SearchResult object) {
                            String name = object.getEntryInfo().getOwner();
                            if (name == null)
                                return SafeHtmlUtils.EMPTY_SAFE_HTML;

                            return SafeHtmlUtils
                                    .fromSafeConstant("<i style=\"width: "
                                                              + width + "px; "
                                                              + "white-space: nowrap; overflow: hidden; text-overflow: "
                                                              + "ellipsis;\" title=\""
                                                              + name.replaceAll("\"", "'") + "\">"
                                                              + name + "</i>");
                        }
                    };

            this.addColumn(nameColumn, "Owner");
            nameColumn.setSortable(false);
            this.setColumnWidth(nameColumn, width, unit);
            return nameColumn;
        }
    }
}