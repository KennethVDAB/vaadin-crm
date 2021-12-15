package com.vaadin.tutorial.crm.ui.view.list;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.tutorial.crm.backend.entity.Company;
import com.vaadin.tutorial.crm.backend.entity.Contact;
import com.vaadin.tutorial.crm.backend.service.CompanyService;
import com.vaadin.tutorial.crm.backend.service.ContactService;
import com.vaadin.tutorial.crm.ui.MainLayout;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//Maps the view to the route
@Component
@Scope("prototype")
@Route(value = "", layout = MainLayout.class)
@PageTitle("Contacts | Vaadin CRM")
public class ListView extends VerticalLayout {

    private final ContactService contactService;
    private final CompanyService companyService;

    final Grid<Contact> grid = new Grid<>(Contact.class);
    private final TextField filterText = new TextField();
    final ContactForm form;

    public ListView(ContactService contactService, CompanyService companyService) {
        this.contactService = contactService;
        this.companyService = companyService;
        addClassName("list-view");
        setSizeFull();

        configureGrid();

        form = new com.vaadin.tutorial.crm.ui.view.list.ContactForm(companyService.findAll());
        form.addListener(com.vaadin.tutorial.crm.ui.view.list.ContactForm.SaveEvent.class, this::saveContact);
        form.addListener(com.vaadin.tutorial.crm.ui.view.list.ContactForm.DeleteEvent.class, this::deleteContact);
        form.addListener(com.vaadin.tutorial.crm.ui.view.list.ContactForm.CloseEvent.class, e -> closeEditor());
        closeEditor();

        Div content = new Div(grid, form);
        content.addClassName("content");
        content.setSizeFull();

        add(getToolbar(), content);
        updateList();
    }

    private void closeEditor() {
        form.setContact(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void configureGrid() {
        grid.addClassName("contact-grid");
        grid.setSizeFull();
        grid.removeColumnByKey("company"); // Removes default column definition
        grid.setColumns("firstName", "lastName", "email", "status");
        grid.addColumn(contact -> {
            Company company = contact.getCompany();
            return company == null ? "-" : company.getName();
        }).setHeader("Company");
        grid.getColumns().forEach(contactColumn -> contactColumn.setAutoWidth(true)); // Automatic with for each column

        grid.asSingleSelect().addValueChangeListener(event ->
                editContact(event.getValue()));
    }

    private void addContact(){
        grid.asSingleSelect().clear();
        editContact(new Contact());
    }

    private void saveContact(com.vaadin.tutorial.crm.ui.view.list.ContactForm.SaveEvent event) {
        contactService.save(event.getContact());
        updateList();
        closeEditor();
    }
    private void deleteContact(com.vaadin.tutorial.crm.ui.view.list.ContactForm.DeleteEvent event) {
        contactService.delete(event.getContact());
        updateList();
        closeEditor();
    }

    public void editContact(Contact contact) {
        if (contact == null) {
            closeEditor();
        } else {
            form.setContact(contact);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY); // Textfield will notify if changes take place after typing.
        filterText.addValueChangeListener(e -> updateList());

        Button addContactButton = new Button("Add Contact");
        addContactButton.addClickListener(buttonClickEvent -> addContact());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void updateList() {
        grid.setItems(contactService.findAll(filterText.getValue()));
    }
}
