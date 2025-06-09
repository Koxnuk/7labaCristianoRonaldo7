package com.example.currency.controller;

import com.example.currency.models.CurrencyInfo;
import com.example.currency.service.CurrencyConversionService;
import com.example.currency.service.CurrencyService;
import com.example.currency.service.RequestCounter;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Tag(name = "Currency Info", description = "API for managing currency information")
public class CurrencyInfoController {

    private final CurrencyService currencyService;
    private final RequestCounter requestCounter;
    private final CurrencyConversionService conversionService;

    @Autowired
    public CurrencyInfoController(CurrencyService currencyService, RequestCounter requestCounter, CurrencyConversionService conversionService) {
        this.currencyService = currencyService;
        this.requestCounter = requestCounter;
        this.conversionService = conversionService;
    }

    @GetMapping("/api/currency/info")
    @Operation(summary = "Get all currencies", description = "Returns a list of all available currencies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currencies"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyInfo>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/api/currency/info/db")
    @Operation(summary = "Get all currencies from database", description = "Returns a list of all currencies stored in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currencies"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyInfo>> getAllCurrenciesFromDb() {
        return ResponseEntity.ok(currencyService.getAllCurrenciesFromDb());
    }

    @GetMapping("/api/currency/info/{id}")
    @Operation(summary = "Get currency by ID", description = "Returns a currency by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currency"),
            @ApiResponse(responseCode = "404", description = "Currency not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyInfo> getCurrencyById(@PathVariable Integer id) {
        Optional<CurrencyInfo> currency = currencyService.getCurrencyById(id);
        if (currency.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Currency not found with ID: " + id);
        }
        return ResponseEntity.ok(currency.get());
    }

    @PostMapping(value = {"/api/currency/info", "/currencies"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @Operation(summary = "Create a new currency", description = "Creates a new currency entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object createCurrency(@Valid @RequestBody(required = false) CurrencyInfo currencyInfo,
                                @ModelAttribute("currency") @Valid CurrencyInfo currencyModel,
                                @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        CurrencyInfo currency = currencyInfo != null ? currencyInfo : currencyModel;
        CurrencyInfo created = currencyService.createCurrency(currency);
        if (acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            return "redirect:/currencies";
        }
        return ResponseEntity.ok(created);
    }

    @PutMapping(value = "/api/currency/info/{id}")
    @PostMapping(value = "/currencies/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Update a currency", description = "Updates an existing currency by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency updated successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object updateCurrency(@PathVariable Integer id,
                                @Valid @RequestBody(required = false) CurrencyInfo currencyInfo,
                                @ModelAttribute("currency") @Valid CurrencyInfo currencyModel,
                                @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        CurrencyInfo currency = currencyInfo != null ? currencyInfo : currencyModel;
        CurrencyInfo updated = currencyService.updateCurrency(id, currency);
        if (acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            return "redirect:/currencies";
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/api/currency/info/{id}")
    @PostMapping(value = "/currencies/{id}/delete")
    @Operation(summary = "Delete a currency", description = "Deletes a currency by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Currency deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object deleteCurrency(@PathVariable Integer id,
                                @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        currencyService.deleteCurrency(id);
        if (acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            return "redirect:/currencies";
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/currency/info/counter")
    @Operation(summary = "Get request counter", description = "Returns the current number of requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved counter value"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Integer> getCounter() {
        return ResponseEntity.ok(requestCounter.getCount());
    }

    @PostMapping("/api/currency/info/reset-counter")
    @Operation(summary = "Reset request counter", description = "Resets the request counter to zero")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Counter reset successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> reset resetCounter() {
        requestCounter.reset();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/currencies")
    public String listCurrencies(Model model) {
        model.addAttribute("currencies", currencyService.getAllCurrenciesFromDb());
        return "currencies/list";
    }

    @GetMapping("/currencies/new")
    public String newCurrencyForm(Model model) {
        model.addAttribute("currency", new CurrencyInfo());
        return "currencies/new";
    }

    @GetMapping("/currencies/{id}")
    public String viewCurrency(@PathVariable Integer id, Model model) {
        CurrencyInfo currency = currencyService.getCurrencyByIdWithRates(id).orElseThrow();
        model.addAttribute("currency", currency);
        return "currencies/view";
    }

    @GetMapping("/currencies/{id}/edit")
    public String editCurrencyForm(@PathVariable Integer id, Model model) {
        CurrencyInfo currency = currencyService.getCurrencyById(id).orElseThrow();
        model.addAttribute("currency", currency);
        return "currencies/edit";
    }

    @GetMapping("/currencies/convert")
    public String convertForm(Model model) {
        model.addAttribute("currencies", currencyService.getAllCurrenciesFromDb());
        return "convert";
    }

    @PostMapping("/currencies/convert")
    public String convert(
            @RequestParam Integer from,
            @RequestParam Integer to,
            @RequestParam BigDecimal amount,
            Model model) {
        Map<String, Object> result = conversionService.convertCurrencyWithValidation(from, to, amount);
        model.addAttribute("result", result.get("result"));
        model.addAttribute("from", result.get("from"));
        model.addAttribute("to", result.get("to"));
        model.addAttribute("amount", result.get("amount"));
        model.addAttribute("currencies", currencyService.getAllCurrenciesFromDb());
        return "convert";
    }
}
