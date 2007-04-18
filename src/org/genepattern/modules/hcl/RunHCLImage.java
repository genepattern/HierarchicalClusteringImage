/*
 The Broad Institute
 SOFTWARE COPYRIGHT NOTICE AGREEMENT
 This software and its documentation are copyright (2003-2006) by the
 Broad Institute/Massachusetts Institute of Technology. All rights are
 reserved.

 This software is supplied without any warranty or guaranteed support
 whatsoever. Neither the Broad Institute nor MIT can be responsible for its
 use, misuse, or functionality.
 */

package org.genepattern.modules.hcl;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JPanel;

import org.genepattern.clustering.hierarchical.AtrGtrReader;
import org.genepattern.clustering.hierarchical.FeatureTreePanel;
import org.genepattern.clustering.hierarchical.SampleTreePanel;
import org.genepattern.heatmap.HeatMap;
import org.genepattern.io.ParseException;
import org.genepattern.io.stanford.CdtParser;
import org.genepattern.matrix.Dataset;
import org.genepattern.module.AnalysisUtil;
import org.genepattern.modules.heatmap.RunHeatMapImage;

public class RunHCLImage extends RunHeatMapImage {
    private AtrGtrReader atrReader;

    private AtrGtrReader gtrReader;

    private CdtParser cdtParser = new CdtParser();

    protected HeatMap createHeatMap() {
        int featureTreeWidth = 150;
        int sampleTreeHeight = 150;
        FeatureTreePanel featureTree = null;
        if (gtrReader != null) {
            featureTree = new FeatureTreePanel(gtrReader);
            featureTree.setElementHeight(rowSize);
            Dimension size = new Dimension(featureTreeWidth, featureTree.getPreferredSize().height);
            featureTree.setPreferredSize(size);
            featureTree.setSize(size);
        }
        SampleTreePanel sampleTree = null;
        if (atrReader != null) {
            sampleTree = new SampleTreePanel(atrReader);
            sampleTree.setElementWidth(columnSize);
            Dimension size = new Dimension(sampleTree.getPreferredSize().width, sampleTreeHeight);
            sampleTree.setPreferredSize(size);
            sampleTree.setSize(size);
        }
        return new HeatMap(new JPanel(), data, featureTree, sampleTree);
    }

    protected void parseArg(String arg, String value) {
        if (arg.equals("-x")) {
            parseGtr(value);
        } else if (arg.equals("-y")) {
            parseAtr(value);
        } else {
            throw new IllegalArgumentException();
        }

    }

    protected Dataset parseDataset() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(inputFileName);
            cdtParser.parse(fis);
        } catch (IOException e) {
            AnalysisUtil.exit("An error occurred while reading the file " + new File(inputFileName).getName() + ".");
        } catch (ParseException e) {
            String message = e.getMessage();
            String error = "An error occurred while reading the file " + new File(inputFileName).getName() + ".";
            if (message != null && message.length() > 0) {
                error += "\n" + message;
            }
            AnalysisUtil.exit(error);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
            }
        }
        return cdtParser.getDataset();
    }

    private void parseAtr(String atrFile) {
        if (atrFile != null) {
            try {
                atrReader = new AtrGtrReader(cdtParser.getArrayIds(), atrFile);
            } catch (IOException e) {
                AnalysisUtil.exit("An error occurred while reading the file " + new File(atrFile).getName() + ".");
            }
        }
    }

    private void parseGtr(String gtrFile) {
        if (gtrFile != null) {
            try {
                gtrReader = new AtrGtrReader((String[]) cdtParser.getGeneIds().toArray(new String[0]), gtrFile);
            } catch (IOException e) {
                AnalysisUtil.exit("An error occurred while reading the file " + new File(gtrFile).getName() + ".");
            }
        }
    }

    public static void main(String[] args) {
        new RunHCLImage().parse(args);
    }

}