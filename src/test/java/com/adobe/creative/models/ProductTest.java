package com.adobe.creative.models;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product model.
 * 
 * @author Ugur Köysüren
 */
class ProductTest {

    @Test
    void testGetLocalizedNameDefaultsToEnglish() {
        Product product = new Product();
        product.setName("English Name");
        
        String name = product.getLocalizedName("fr-FR");
        
        assertEquals("English Name", name);
    }

    @Test
    void testGetLocalizedNameReturnsGerman() {
        Product product = new Product();
        product.setName("English Name");
        
        Map<String, ProductLocalization> localizations = new HashMap<>();
        ProductLocalization germanLoc = new ProductLocalization();
        germanLoc.setName("German Name");
        localizations.put("de-DE", germanLoc);
        
        product.setLocalizations(localizations);
        
        String name = product.getLocalizedName("de-DE");
        
        assertEquals("German Name", name);
    }

    @Test
    void testGetLocalizedDescriptionDefaultsToEnglish() {
        Product product = new Product();
        product.setDescription("English Description");
        
        String desc = product.getLocalizedDescription("es-ES");
        
        assertEquals("English Description", desc);
    }

    @Test
    void testGetLocalizedDescriptionReturnsLocalized() {
        Product product = new Product();
        product.setDescription("English Description");
        
        Map<String, ProductLocalization> localizations = new HashMap<>();
        ProductLocalization germanLoc = new ProductLocalization();
        germanLoc.setDescription("German Description");
        localizations.put("de-DE", germanLoc);
        
        product.setLocalizations(localizations);
        
        String desc = product.getLocalizedDescription("de-DE");
        
        assertEquals("German Description", desc);
    }

    @Test
    void testGetLocalizedWithNullLocalizations() {
        Product product = new Product();
        product.setName("Default Name");
        product.setDescription("Default Description");
        product.setLocalizations(null);
        
        assertEquals("Default Name", product.getLocalizedName("de-DE"));
        assertEquals("Default Description", product.getLocalizedDescription("de-DE"));
    }

    @Test
    void testGetLocalizedWithEmptyLocalization() {
        Product product = new Product();
        product.setName("Default Name");
        
        Map<String, ProductLocalization> localizations = new HashMap<>();
        ProductLocalization emptyLoc = new ProductLocalization();
        localizations.put("de-DE", emptyLoc);
        
        product.setLocalizations(localizations);
        
        String name = product.getLocalizedName("de-DE");
        assertEquals("Default Name", name);
    }
}

