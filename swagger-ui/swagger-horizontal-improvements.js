// Swagger UI Horizontal Space Improvements
// This script enhances the display of deeply nested schemas to use horizontal space more effectively

(function() {
    'use strict';

    // Wait for the DOM to be ready
    function ready(fn) {
        if (document.readyState !== 'loading') {
            fn();
        } else {
            document.addEventListener('DOMContentLoaded', fn);
        }
    }

    // Function to improve schema display
    function improveSchemaDisplay() {
        // Add horizontal scrolling to schema containers
        const schemaContainers = document.querySelectorAll('.swagger-ui .model, .swagger-ui .models, .swagger-ui .model-box');
        schemaContainers.forEach(container => {
            container.style.overflowX = 'auto';
            container.style.maxWidth = 'none';
            container.style.width = '100%';
        });

        // Prevent wrapping in property names and types
        const propertyElements = document.querySelectorAll('.swagger-ui .prop-name, .swagger-ui .prop-type, .swagger-ui .prop-format');
        propertyElements.forEach(element => {
            element.style.whiteSpace = 'nowrap';
            element.style.flexShrink = '0';
        });

        // Make deeply nested objects display better
        const nestedObjects = document.querySelectorAll('.swagger-ui .model .object, .swagger-ui .model .array');
        nestedObjects.forEach(obj => {
            obj.style.overflowX = 'auto';
            obj.style.maxWidth = 'none';
        });

        // Improve property row display
        const propertyRows = document.querySelectorAll('.swagger-ui .property-row, .swagger-ui .model .property');
        propertyRows.forEach(row => {
            row.style.whiteSpace = 'nowrap';
            row.style.overflowX = 'auto';
            row.style.display = 'flex';
            row.style.flexWrap = 'nowrap';
        });

        // Improve example displays
        const examples = document.querySelectorAll('.swagger-ui .model .example, .swagger-ui .example');
        examples.forEach(example => {
            example.style.overflowX = 'auto';
            example.style.whiteSpace = 'pre';
            example.style.maxWidth = 'none';
        });

        // Fix the specific renderedMarkdown issue in descriptions
        const renderedMarkdownElements = document.querySelectorAll('.swagger-ui .description .renderedMarkdown');
        renderedMarkdownElements.forEach(element => {
            element.style.whiteSpace = 'normal';
            element.style.wordWrap = 'break-word';
            element.style.wordBreak = 'normal';
            element.style.overflowWrap = 'break-word';
            element.style.maxWidth = 'none';
            element.style.width = 'auto';
            element.style.minWidth = '0';

            // Also fix child elements
            const childElements = element.querySelectorAll('p, div, span');
            childElements.forEach(child => {
                child.style.whiteSpace = 'normal';
                child.style.wordWrap = 'break-word';
                child.style.wordBreak = 'normal';
                child.style.overflowWrap = 'break-word';
                child.style.display = 'block';
                child.style.width = 'auto';
                child.style.maxWidth = 'none';
            });
        });

        // Fix description containers
        const descriptions = document.querySelectorAll('.swagger-ui .description');
        descriptions.forEach(desc => {
            desc.style.maxWidth = 'none';
            desc.style.width = 'auto';
            desc.style.flexBasis = 'auto';
            desc.style.minWidth = '0';
            desc.style.flex = '1 1 auto';
            if (desc.parentElement && desc.parentElement.classList.contains('property')) {
                desc.style.minWidth = '300px';
            }
        });

        // Fix property layout to prevent shifting when nested schemas expand
        const properties = document.querySelectorAll('.swagger-ui .model .property');
        properties.forEach(property => {
            property.style.display = 'flex';
            property.style.flexDirection = 'column';
            property.style.alignItems = 'stretch';
        });

        // Ensure property rows have correct layout
        const propertyRows = document.querySelectorAll('.swagger-ui .property .property-row');
        propertyRows.forEach(row => {
            row.style.display = 'flex';
            row.style.flexDirection = 'row';
            row.style.alignItems = 'flex-start';
            row.style.width = '100%';
        });

        // Fix nested content positioning
        const nestedContent = document.querySelectorAll('.swagger-ui .property .inner-object, .swagger-ui .property .model, .swagger-ui .property .object');
        nestedContent.forEach(nested => {
            nested.style.width = '100%';
            nested.style.marginTop = '10px';
            nested.style.marginLeft = '20px';
            nested.style.position = 'relative';
            nested.style.clear = 'both';
        });

        // Ensure descriptions in property rows stay in position
        const propertyRowDescriptions = document.querySelectorAll('.swagger-ui .property .property-row .description');
        propertyRowDescriptions.forEach(desc => {
            desc.style.position = 'relative';
            desc.style.flex = '1 1 auto';
            desc.style.minWidth = '300px';
            desc.style.maxWidth = 'none';
            desc.style.marginLeft = '0';
            desc.style.marginRight = '0';
        });
        
        // Fix the double scrollbar issue with model-box-control
        const modelBoxControls = document.querySelectorAll('.swagger-ui .model-box-control');
        modelBoxControls.forEach(control => {
            control.style.overflow = 'visible';
            control.style.overflowX = 'visible';
            control.style.overflowY = 'visible';
            
            // Also fix the model-box itself
            const modelBox = control.querySelector('.model-box');
            if (modelBox) {
                modelBox.style.overflowX = 'auto';
                modelBox.style.overflowY = 'visible';
            }
        });
        
        // Fix model containers to prevent duplicate scrollbars
        const modelContainers = document.querySelectorAll('.swagger-ui .model-container');
        modelContainers.forEach(container => {
            container.style.overflowX = 'auto';
            container.style.overflowY = 'visible';
        });
        
        // Fix schema button alignment in the schemas section
        const schemaButtons = document.querySelectorAll('.swagger-ui .models .model-container .model-box-control');
        schemaButtons.forEach(button => {
            button.style.display = 'flex';
            button.style.alignItems = 'center';
            button.style.whiteSpace = 'nowrap';
            button.style.width = '100%';
            button.style.paddingRight = '8px';
            
            // Add spacer element if it doesn't exist
            if (!button.querySelector('.schema-spacer')) {
                const spacer = document.createElement('span');
                spacer.className = 'schema-spacer';
                spacer.style.flex = '1 1 auto';
                spacer.style.minWidth = '8px';
                spacer.style.maxWidth = '8px';
                spacer.style.height = '0';
                spacer.style.lineHeight = '0';
                spacer.style.alignSelf = 'center';
                
                // Find the last title element and insert spacer after it
                const titleElements = button.querySelectorAll('.model-box-control-title, .model-title, .title');
                const lastTitle = titleElements[titleElements.length - 1];
                if (lastTitle && lastTitle.parentNode === button) {
                    lastTitle.insertAdjacentElement('afterend', spacer);
                }
            }
            
            // Fix title elements within the button
            const titleElements = button.querySelectorAll('.model-box-control-title, .model-title, .title');
            titleElements.forEach(title => {
                title.style.display = 'flex';
                title.style.alignItems = 'center';
                title.style.flex = '0 0 auto';
                title.style.whiteSpace = 'nowrap';
                title.style.marginRight = '0';
            });
            
            // Fix toggle/arrow elements
            const toggleElements = button.querySelectorAll('.model-toggle, .arrow, .chevron');
            toggleElements.forEach(toggle => {
                toggle.style.display = 'flex';
                toggle.style.alignItems = 'center';
                toggle.style.justifyContent = 'center';
                toggle.style.flexShrink = '0';
                toggle.style.marginLeft = '0';
                toggle.style.whiteSpace = 'nowrap';
                toggle.style.height = '100%';
                toggle.style.lineHeight = '1';
                toggle.style.alignSelf = 'center';
            });
            
            // Ensure all direct child elements are inline-flex
            Array.from(button.children).forEach(child => {
                if (child.tagName === 'SPAN' || child.tagName === 'DIV') {
                    if (!child.classList.contains('schema-spacer')) {
                        child.style.display = 'inline-flex';
                        child.style.alignItems = 'center';
                        child.style.whiteSpace = 'nowrap';
                    }
                }
            });
        });
    }

    // Function to observe for dynamic content changes
    function observeSwaggerUI() {
        const observer = new MutationObserver(function(mutations) {
            let shouldUpdate = false;
            mutations.forEach(function(mutation) {
                if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                    // Check if any added nodes contain swagger-ui elements
                    mutation.addedNodes.forEach(function(node) {
                        if (node.nodeType === 1) { // Element node
                            if (node.classList && (node.classList.contains('swagger-ui') || node.querySelector('.swagger-ui'))) {
                                shouldUpdate = true;
                            }
                        }
                    });
                }
            });

            if (shouldUpdate) {
                setTimeout(improveSchemaDisplay, 100); // Small delay to ensure DOM is updated
            }
        });

        // Start observing
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }

    // Initialize when DOM is ready
    ready(function() {
        // Initial improvement
        setTimeout(improveSchemaDisplay, 500); // Wait for Swagger UI to initialize

        // Set up observer for dynamic changes
        observeSwaggerUI();

        // Also trigger on window resize
        window.addEventListener('resize', function() {
            setTimeout(improveSchemaDisplay, 100);
        });

        // Trigger improvements periodically for dynamic content
        setInterval(improveSchemaDisplay, 2000);
    });
})();
