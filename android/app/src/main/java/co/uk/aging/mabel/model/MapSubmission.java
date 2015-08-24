package co.uk.aging.mabel.model;

import android.text.TextUtils;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import co.uk.aging.mabel.utils.DBConstants;

/**
 * Data model for post
 * Created by Ryan McClarnon (ryan@breezie.com) on 22/08/15.
 */
@ParseClassName("Submission")
public class MapSubmission extends ParseObject {
    public MapSubmission() {
    }

    public String getDescription() {
        return getString(DBConstants.DESCRIPTION);
    }
    public void setDescription(String description) {
        if (!TextUtils.isEmpty(description)) {
            this.put(DBConstants.DESCRIPTION, description);
        }
    }

    public ParseGeoPoint getGeoPoint() {
        return getParseGeoPoint(DBConstants.GEO_POINT);
    }
    public void setGeoPoint(ParseGeoPoint geoPoint) {
        if (geoPoint != null) {
            this.put(DBConstants.GEO_POINT, geoPoint);
        }
    }

    public ParseFile getFile() {
        return getParseFile(DBConstants.FILE);
    }
    public void setFile(ParseFile file) {
        if (file != null) {
            this.put(DBConstants.FILE, file);
        }
    }

    public int getProblemOrSolution() {
        return getInt(DBConstants.PROBLEM_OR_SOLUTION);
    }
    public void setProblemOrSolution(int problemOrSolution) {
        this.put(DBConstants.PROBLEM_OR_SOLUTION, problemOrSolution);
    }

    public String getProblemOrSolutionText() {
        return getString(DBConstants.PROBLEM_OR_SOLUTION_TEXT);
    }
    public void setProblemOrSolutionText(String problemOrSolutionText) {
        if (!TextUtils.isEmpty(problemOrSolutionText)) {
            this.put(DBConstants.PROBLEM_OR_SOLUTION_TEXT, problemOrSolutionText);
        }
    }

    public static ParseQuery<MapSubmission> getQuery() {
        return ParseQuery.getQuery(MapSubmission.class);
    }

}
