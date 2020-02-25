package com.astoev.cave.survey.util;

import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.Workspace;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gallery related logic.
 */
public class GalleryUtil {

    private static final List<Character> GALLERY_LETTERS = new ArrayList<>();
    static {
        for (char c = 'A'; c <= 'Z'; c++) {
            GALLERY_LETTERS.add(c);
        }
    }

    // A -> B ... -> Z -> AA -> AB  ... -> AZ -> BA ... etc for next galleries
    public static String generateNextGalleryName() throws SQLException {
        Integer currProjectId = Workspace.getCurrentInstance().getActiveProjectId();
        Gallery lastGallery = DaoUtil.getLastGallery(currProjectId);
        return generateNextGalleryName(lastGallery.getName());
    }

    public static String generateNextGalleryName(String aName) {
        // make the name into number
        char[] letters = new StringBuilder(aName).reverse().toString().toCharArray();
        int num = 0, position = 0, increment;
        for (char c : letters) {
            increment = position == 0 ? 1 : (int) Math.pow(GALLERY_LETTERS.size(), position);
            num += (GALLERY_LETTERS.indexOf(c) + 1) * increment;
            position ++;
        }

        // increment
        num ++;

        // encode as letters
        StringBuilder nextName = new StringBuilder();
        int wholePart, remainder;
        while (num > 0) {
            if (num <= GALLERY_LETTERS.size()) {
                nextName.append(GALLERY_LETTERS.get(num - 1));
                break;
            }
            wholePart = num / GALLERY_LETTERS.size();
            remainder = num % GALLERY_LETTERS.size();

            if (remainder == 0) {
                nextName.append(GALLERY_LETTERS.get(GALLERY_LETTERS.size() - 1));
                wholePart --;
            } else {
                nextName.append(GALLERY_LETTERS.get(remainder - 1));
            }
            num = wholePart;
        }

        return nextName.reverse().toString();
    }

    // "A" as starting
    public static String getFirstGalleryName() {
        return "A";
    }

    public static Gallery createGallery(boolean isFirst) throws SQLException {
        Project currProject = Workspace.getCurrentInstance().getActiveProject();
        String name;
        if (isFirst) {
            name = getFirstGalleryName();
        } else {
            name = generateNextGalleryName();
        }
        return createGallery(currProject, name);
    }

    public static Gallery createGallery(Project aProject, String aName) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(aName);
        gallery.setProject(aProject);
        Workspace.getCurrentInstance().getDBHelper().getGalleryDao().create(gallery);
        return gallery;
    }

}
