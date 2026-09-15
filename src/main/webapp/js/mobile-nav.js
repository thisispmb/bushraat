/* Mobile navigation */
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".sidebar").forEach((sidebar) => {
        const nav = sidebar.querySelector(".nav");
        const toggle = sidebar.querySelector(".mobile-menu-toggle");

        if (!nav || !toggle) {
            return;
        }

        const closeMenu = () => {
            sidebar.classList.remove("nav-open");
            nav.style.setProperty("display", "none", "important");
            toggle.setAttribute("aria-expanded", "false");
            toggle.setAttribute("aria-label", "Open navigation");
        };

        const openMenu = () => {
            sidebar.classList.add("nav-open");
            nav.style.setProperty("display", "flex", "important");
            toggle.setAttribute("aria-expanded", "true");
            toggle.setAttribute("aria-label", "Close navigation");
        };

        const syncMenu = () => {
            if (window.innerWidth <= 760) {
                if (!sidebar.classList.contains("nav-open")) {
                    nav.style.setProperty("display", "none", "important");
                }
            } else {
                nav.style.removeProperty("display");
                sidebar.classList.remove("nav-open");
                toggle.setAttribute("aria-expanded", "false");
                toggle.setAttribute("aria-label", "Open navigation");
            }
        };

        toggle.addEventListener("click", () => {
            if (sidebar.classList.contains("nav-open")) {
                closeMenu();
            } else {
                openMenu();
            }
        });

        nav.querySelectorAll("a").forEach((link) => {
            link.addEventListener("click", closeMenu);
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                closeMenu();
            }
        });

        window.addEventListener("resize", syncMenu);

        syncMenu();
    });
});
