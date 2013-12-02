package com.erigir.lucid;

import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * cweiss : 5/26/12 1:26 PM
 */
public class SearchPanel extends JPanel implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SearchPanel.class);

    private JTextArea query = new JTextArea("select 1");
    private JTextField targetDirectory = new JTextField("lucidRelationOutput");
    private JButton runProcess = new JButton("Run Query");
    private JTable outputTable = new JTable();

    @Override
    public void afterPropertiesSet() throws Exception {

        JPanel queryPanel = new JPanel(new GridLayout(0,2));

        queryPanel.add(new JLabel("Target Directory"));
        queryPanel.add(targetDirectory);

        queryPanel.add(new JLabel("Lucene Query"));
        query.setPreferredSize(new Dimension(400, 100));
        JScrollPane scrollPane = new JScrollPane(query);
        queryPanel.add(scrollPane);

        outputTable.setPreferredScrollableViewportSize(new Dimension(400,100));
        JScrollPane outputTableScroll = new JScrollPane(outputTable);

        setLayout(new BorderLayout());
        add(queryPanel, BorderLayout.NORTH);
        add(outputTableScroll, BorderLayout.CENTER);
        add(runProcess, BorderLayout.SOUTH);

        runProcess.addActionListener(new LuceneQueryAction());

    }

    class LuceneQueryAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try
            {
                    // Create a sample in-memory db
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        Directory index = new NIOFSDirectory(new File(targetDirectory.getText()));

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);

                String querystr = StringUtils.trimToEmpty(query.getText());
             Query q = new QueryParser(Version.LUCENE_46, LuceneIndexingRowCallbackHandler.ALL_FIELD_NAME, analyzer).parse(querystr);

             int hitsPerPage = 10;
             IndexReader reader = IndexReader.open(index);
             IndexSearcher searcher = new IndexSearcher(reader);
             TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
             searcher.search(q, collector);
             ScoreDoc[] hits = collector.topDocs().scoreDocs;

             StringBuilder sb = new StringBuilder();
             sb.append("Found " + hits.length + " hits\n");
             for(int i=0;i<hits.length;++i) {
                 int docId = hits[i].doc;
                 Document d = searcher.doc(docId);
                 for (IndexableField f:d.getFields())
                 {
                     sb.append(f.name()).append(" = ").append(f.stringValue()).append(" .. ");
                 }
                 sb.append("\n");
             }
             JOptionPane.showMessageDialog(null,sb.toString());

            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null,"Error searching"+e);
            }
        }
    }

}
