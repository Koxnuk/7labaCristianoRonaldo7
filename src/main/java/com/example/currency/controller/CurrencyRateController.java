package com.example.currency.controller;

import com.example.currency.models.CurrencyInfo;
import com.example.currency.models.CurrencyRate;
import com.example.currency.service.CurrencyConversionService;
import com.example.currency.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Tag(name = "Currency Rates", description = "API for managing currency rates and conversions")
public class CurrencyRateController {

    private final CurrencyConversionService conversionService;
    private final CurrencyService currencyService;

    @Autowired
    public CurrencyRateController(CurrencyConversionService conversionService, CurrencyService currencyService) {
        this.conversionService = conversionService;
        this.currencyService = currencyService;
    }

    @GetMapping("/api/currency/rates/convert")
    @Operation(summary = "Convert currency", description = "Converts an amount from one currency to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversion successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input or amount less than or equal to zero"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> convert(
            @RequestParam Integer from,
            @RequestParam Integer to,
            @RequestParam BigDecimal amount) {
        try {
            Map<String, Object> result = conversionService.convertCurrencyWithValidation(from, to, amount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new IllegalArgumentException("Conversion error: " + e.getMessage());
        }
    }

    @GetMapping("/api/currency/rates")
    @Operation(summary = "Get all currency rates", description = "Returns a list of all currency rates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rates"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyRate>> getAllRates() {
        return ResponseEntity.ok(conversionService.getAllRates());
    }

    @GetMapping("/api/currency/rates/{id}")
    @Operation(summary = "Get currency rate by ID", description = "Returns a currency rate by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rate"),
            @ApiResponse(responseCode = "404", description = "Rate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyRate> getRateById(@PathVariable Long id) {
        Optional<CurrencyRate> rate = conversionService.getRateById(id);
        if (rate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate not found with ID: " + id);
        }
        return ResponseEntity.ok(rate.get());
    }

    @PostMapping(value = {"/api/currency/rates", "/rates"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @Operation(summary = "Create a new currency rate", description = "Creates a new currency rate entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object createRate(@Valid @RequestBody(required = false) CurrencyRate rate,
                            @ModelAttribute("rate") @Valid CurrencyRate rateModel,
                            @RequestParam(required = false) Integer currencyId,
                            @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        CurrencyRate targetRate = rate != null ? rate : rateModel;
        if (currencyId != null) {
            CurrencyInfo currency = currencyService.getCurrencyById(currencyId).orElseThrow();
            targetRate.setCurrency(currency);
        }
        CurrencyRate created = conversionService.createRate(targetRate);
        if (acceptHeader.contains(MediaType.TEXT_HTML_VALUE) && currencyId != null) {
            return "redirect:/currencies/" + currencyId;
        }
        return ResponseEntity.ok(created);
    }

    @PutMapping(value = "/api/currency/rates/{id}")
    @PostMapping(value = "/rates/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Update a currency rate", description = "Updates an existing currency rate by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rate updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rate not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object updateRate(@PathVariable Long id,
                            @Valid @RequestBody(required = false) CurrencyRate rate,
                            @ModelAttribute("rate") @Valid CurrencyRate rateModel,
                            @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        CurrencyRate targetRate = rate != null ? rate : rateModel;
        CurrencyRate updated = conversionService.updateRate(id, targetRate);
        if (acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            return "redirect:/currencies/" + updated.getCurrency().getCurId();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/api/currency/rates/{id}")
    @PostMapping(value = "/rates/{id}/delete")
    @Operation(summary = "Delete a currency rate", description = "Deletes a currency rate by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object deleteRate(@PathVariable Long id,
                            @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        CurrencyRate rate = conversionService.getRateById(id).orElseThrow();
        Integer currencyId = rate.getCurrency().getCurId();
        conversionService.deleteRate(id);
        if (acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            return "redirect:/currencies/" + currencyId;
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/currency/rates/by-abbreviation")
    @Operation(summary = "Get rates by abbreviation and date", description = "Returns currency rates for a specific abbreviation and date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rates"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyRate>> getRatesByAbbreviationAndDate(
            @RequestParam String abbreviation,
            @RequestParam LocalDate date) {
        List<CurrencyRate> rates = conversionService.getRatesByAbbreviationAndDate(abbreviation, date);
        return ResponseEntity.ok(rates);
    }

    @PostMapping("/api/currency/rates/bulk-rates")
    @Operation(summary = "Get rates for multiple currencies", description = "Returns rates for a list of currency abbreviations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rates"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyRate>> getBulkRates(@RequestBody List<String> abbreviations) {
        List<CurrencyRate> rates = conversionService.getBulkRates(abbreviations);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/rates/new")
    public String newRateForm(@RequestParam Integer currencyId, Model model) {
        CurrencyRate rate = new CurrencyRate();
        model.addAttribute("rate", rate);
        model.addAttribute("currencyId", currencyId);
        return "rates/new";
    }

    @GetMapping("/rates/{id}/edit")
    public String editRateForm(@PathVariable Long id, Model model) {
        CurrencyRate rate = conversionService.getRateById(id).orElseThrow();
        model.addAttribute("rate", rate);
        return "rates/edit";
    }
}
