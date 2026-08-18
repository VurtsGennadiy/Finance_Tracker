package ru.vgd.tracker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vgd.tracker.dal.account.entity.AccountType;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.facade.main.MainFacade;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.dto.transaction.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.transaction.TransferCreateRequest;

import java.util.Collection;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class MainController {
    private final MainFacade mainFacade;

    @GetMapping
    public String indexPage(@AuthenticationPrincipal SecurityUser principal,
                            Model model) {

        var data = mainFacade.getMainPageData(principal.getUserId());

        model.addAttribute("data", data);
        model.addAttribute("accountsList", data.getAccounts().values().stream().flatMap(Collection::stream).toList());
        model.addAttribute("incomeCategories", Category.getIncomeCategories());
        model.addAttribute("expenseCategories", Category.getExpenseCategories());
        model.addAttribute("accountTypes", AccountType.values());

        model.addAttribute("transactionCreateRequest", new TransactionCreateRequest());
        model.addAttribute("transferCreateRequest", new TransferCreateRequest());

        if (!principal.getUser().isConfirmedEmail()) {
            model.addAttribute("emailConfirmedWarning", true);
        }

        if (data.getAccounts().isEmpty()) {
            model.addAttribute("noAccountsInfo", true);
        }

        return "index";
    }
}
