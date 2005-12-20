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

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genepattern.clustering.hierarchical.image.HCLImage;
import org.genepattern.heatmap.image.DisplaySettings;
import org.genepattern.heatmap.image.FeatureAnnotator;
import org.genepattern.heatmap.image.HeatMap;
import org.genepattern.heatmap.RowColorConverter;
import org.genepattern.io.ParseException;
import org.genepattern.module.AnalysisUtil;

public class RunHCLImage {
	public static void main(String[] args) {
		String cdtFile = args[0];
		String outputFileName = args[1];
		String outputFileFormat = args[2];

		String gtrFile = null;
		String atrFile = null;
		int columnWidth = 10;
		int rowWidth = 10;
		int normalization = HeatMap.COLOR_RESPONSE_ROW;
		Color gridLinesColor = Color.black;
		boolean showGridLines = false;
		boolean showGeneAnnotations = false;
		boolean showGeneNames = true;
		List featureList = null;
		Color highlightColor = Color.red;
		Color[] colorMap = null;
		for (int i = 3; i < args.length; i++) { // 0th arg is input file name,
			// 1st arg is output file name,
			// 2nd arg is format
			String arg = args[i].substring(0, 2);
			String value = args[i].substring(2, args[i].length());
			if (value.equals("")) {
				continue;
			}

			if (arg.equals("-x")) {
				gtrFile = value;
			} else if (arg.equals("-y")) {
				atrFile = value;
			} else if (arg.equals("-c")) {
				columnWidth = Integer.parseInt(value);
			} else if (arg.equals("-r")) {
				rowWidth = Integer.parseInt(value);
			} else if (arg.equals("-n")) {
				if (value.equals("global")) {
					normalization = HeatMap.COLOR_RESPONSE_GLOBAL;
				} else if (value.equals("row normalized")) {
					normalization = HeatMap.COLOR_RESPONSE_ROW;
				}

			} else if (arg.equals("-g")) {
				showGridLines = "yes".equalsIgnoreCase(value);
			} else if (arg.equals("-l")) {
				// r:g:b triplet
				gridLinesColor = HeatMap.createColor(value);
			} else if (arg.equals("-a")) {
				showGeneAnnotations = "yes".equalsIgnoreCase(value);
			} else if (arg.equals("-s")) {
				showGeneNames = "yes".equalsIgnoreCase(value);
			} else if (arg.equals("-f")) {
				featureList = AnalysisUtil.readFeatureList(value);
			} else if (arg.equals("-h")) {
				highlightColor = HeatMap.createColor(value);
			} else if (arg.equals("-m")) {
				colorMap = HeatMap.parseColorMap(value);
			} else {
				AnalysisUtil.exit("unknown option " + arg);
			}
		}
		Color[] _colorMap = colorMap != null ? colorMap : RowColorConverter
				.getDefaultColorMap();
		try {
			DisplaySettings ds = new DisplaySettings();
			ds.columnSize = columnWidth;
			ds.rowSize = rowWidth;
			if (normalization == HeatMap.COLOR_RESPONSE_ROW) {
				ds.colorConverter = RowColorConverter.getRowInstance(_colorMap);
			} else {
				ds.colorConverter = RowColorConverter
						.getGlobalInstance(_colorMap);
			}
			ds.drawGrid = showGridLines;
			ds.drawRowNames = showGeneNames;
			ds.drawRowDescriptions = showGeneAnnotations;
			ds.gridLinesColor = gridLinesColor;

			final Map featureNames2Colors = new HashMap();
			if (featureList != null) {
				for (int i = 0; i < featureList.size(); i++) {
					String name = (String) featureList.get(i);
					featureNames2Colors.put(name, highlightColor);
				}

			}
			FeatureAnnotator fa = new FeatureAnnotator() {
				public String getAnnotation(String feature, int j) {
					return null;
				}

				public int getColumnCount() {
					return 0;
				}

				public List getColors(String featureName) {
					return (List) featureNames2Colors.get(featureName);
				}
			};

			HCLImage.saveImage(cdtFile, gtrFile, atrFile, ds, null, fa,
					outputFileName, outputFileFormat);
		} catch (Exception e) {
			if (e instanceof IOException || e instanceof ParseException
					|| e instanceof RuntimeException) {
				AnalysisUtil.exit(e.getMessage());
			} else {
				AnalysisUtil.exit("An error occurred while saving the image.");
			}
		} catch (OutOfMemoryError ome) {
			AnalysisUtil.exit("Not enough memory available to save the image.");
		}
	}
}